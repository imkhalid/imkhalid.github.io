package com.khalid.vyntra.data.repository

import com.khalid.vyntra.data.local.dao.ProductDao
import com.khalid.vyntra.data.local.dao.StockAdjustmentDao
import com.khalid.vyntra.data.mapper.toDomain
import com.khalid.vyntra.data.mapper.toEntity
import com.khalid.vyntra.domain.model.StockAdjustment
import com.khalid.vyntra.domain.repository.StockAdjustmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StockAdjustmentRepositoryImpl @Inject constructor(
    private val stockAdjustmentDao: StockAdjustmentDao,
    private val productDao: ProductDao
) : StockAdjustmentRepository {

    override fun getAll(): Flow<List<StockAdjustment>> {
        return stockAdjustmentDao.getAll().map { entities ->
            entities.map { entity ->
                val product = productDao.getById(entity.productId)
                entity.toDomain(product?.name ?: "Unknown Product")
            }
        }
    }

    override fun getByProduct(productId: Long): Flow<List<StockAdjustment>> {
        return stockAdjustmentDao.getByProduct(productId).map { entities ->
            val product = productDao.getById(productId)
            entities.map { it.toDomain(product?.name ?: "Unknown Product") }
        }
    }

    override suspend fun addAdjustment(adjustment: StockAdjustment): Long {
        return stockAdjustmentDao.insert(adjustment.toEntity())
    }
}
