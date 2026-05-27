package com.khalid.cashlytics.data.repository

import com.khalid.cashlytics.data.local.dao.PurchaseDao
import com.khalid.cashlytics.data.local.dao.PurchaseItemDao
import com.khalid.cashlytics.data.local.dao.VendorDao
import com.khalid.cashlytics.data.mapper.toDomain
import com.khalid.cashlytics.data.mapper.toEntity
import com.khalid.cashlytics.domain.model.Purchase
import com.khalid.cashlytics.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PurchaseRepositoryImpl @Inject constructor(
    private val purchaseDao: PurchaseDao,
    private val purchaseItemDao: PurchaseItemDao,
    private val vendorDao: VendorDao
) : PurchaseRepository {

    override fun getAll(): Flow<List<Purchase>> {
        return purchaseDao.getAll().map { entities ->
            entities.map { entity ->
                val vendorName = entity.vendorId?.let { vendorDao.getById(it)?.name }.orEmpty()
                entity.toDomain(vendorName = vendorName)
            }
        }
    }

    override suspend fun getById(id: Long): Purchase? {
        val purchaseWithItems = purchaseDao.getPurchaseWithItems(id) ?: return null
        val entity = purchaseWithItems.purchase
        val vendorName = entity.vendorId?.let { vendorDao.getById(it)?.name }.orEmpty()
        val items = purchaseWithItems.items.map { it.toDomain() }
        return entity.toDomain(vendorName = vendorName, items = items)
    }

    override fun getByVendor(vendorId: Long): Flow<List<Purchase>> {
        return purchaseDao.getByVendor(vendorId).map { entities ->
            entities.map { entity ->
                val vendorName = entity.vendorId?.let { vendorDao.getById(it)?.name }.orEmpty()
                entity.toDomain(vendorName = vendorName)
            }
        }
    }

    override fun getByDateRange(startDate: Long, endDate: Long): Flow<List<Purchase>> {
        return purchaseDao.getByDateRange(startDate, endDate).map { entities ->
            entities.map { entity ->
                val vendorName = entity.vendorId?.let { vendorDao.getById(it)?.name }.orEmpty()
                entity.toDomain(vendorName = vendorName)
            }
        }
    }

    override suspend fun createPurchase(purchase: Purchase): Long {
        val purchaseId = purchaseDao.insert(purchase.toEntity())
        if (purchase.items.isNotEmpty()) {
            val itemEntities = purchase.items.map { item ->
                item.toEntity().copy(purchaseId = purchaseId)
            }
            purchaseItemDao.insertAll(itemEntities)
        }
        return purchaseId
    }
}
