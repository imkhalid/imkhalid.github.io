package com.khalid.cashlytics.domain.repository

import com.khalid.cashlytics.domain.model.EntityType
import com.khalid.cashlytics.domain.model.PaymentRecord
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    fun getAll(): Flow<List<PaymentRecord>>
    fun getByEntity(entityType: EntityType, entityId: Long): Flow<List<PaymentRecord>>
    suspend fun addPayment(payment: PaymentRecord): Long
}
