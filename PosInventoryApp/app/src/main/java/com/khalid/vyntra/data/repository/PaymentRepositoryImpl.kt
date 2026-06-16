package com.khalid.vyntra.data.repository

import com.khalid.vyntra.data.local.dao.CustomerDao
import com.khalid.vyntra.data.local.dao.PaymentRecordDao
import com.khalid.vyntra.data.local.dao.VendorDao
import com.khalid.vyntra.data.mapper.toDomain
import com.khalid.vyntra.data.mapper.toEntity
import com.khalid.vyntra.domain.model.PaymentRecord
import com.khalid.vyntra.domain.model.EntityType
import com.khalid.vyntra.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    private val paymentRecordDao: PaymentRecordDao,
    private val customerDao: CustomerDao,
    private val vendorDao: VendorDao
) : PaymentRepository {

    override fun getAll(): Flow<List<PaymentRecord>> {
        return paymentRecordDao.getAll().map { entities ->
            entities.map { entity ->
                val entityName = when (EntityType.valueOf(entity.entityType)) {
                    EntityType.CUSTOMER -> customerDao.getById(entity.entityId)?.name ?: "Unknown"
                    EntityType.VENDOR -> vendorDao.getById(entity.entityId)?.name ?: "Unknown"
                }
                entity.toDomain(entityName)
            }
        }
    }

    override fun getByEntity(type: EntityType, entityId: Long): Flow<List<PaymentRecord>> {
        return paymentRecordDao.getByEntity(type.name, entityId).map { entities ->
            val entityName = when (type) {
                EntityType.CUSTOMER -> customerDao.getById(entityId)?.name ?: "Unknown"
                EntityType.VENDOR -> vendorDao.getById(entityId)?.name ?: "Unknown"
            }
            entities.map { it.toDomain(entityName) }
        }
    }

    override suspend fun addPayment(payment: PaymentRecord): Long {
        return paymentRecordDao.insert(payment.toEntity())
    }
}
