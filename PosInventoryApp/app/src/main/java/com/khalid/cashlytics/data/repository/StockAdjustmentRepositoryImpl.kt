package com.khalid.cashlytics.data.repository

import com.khalid.cashlytics.data.local.dao.ProductDao
import com.khalid.cashlytics.data.local.dao.StockAdjustmentDao
import com.khalid.cashlytics.data.mapper.toDomain
import com.khalid.cashlytics.data.mapper.toEntity
import com.khalid.cashlytics.domain.model.StockAdjustment
import com.khalid.cashlytics.domain.repository.StockAdjustmentRepository
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
