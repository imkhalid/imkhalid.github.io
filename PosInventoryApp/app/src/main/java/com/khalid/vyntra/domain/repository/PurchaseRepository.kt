package com.khalid.vyntra.domain.repository

import com.khalid.vyntra.domain.model.Purchase
import kotlinx.coroutines.flow.Flow

interface PurchaseRepository {
    fun getAll(): Flow<List<Purchase>>
    suspend fun getById(id: Long): Purchase?
    fun getByVendor(vendorId: Long): Flow<List<Purchase>>
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<Purchase>>
    suspend fun createPurchase(purchase: Purchase): Long
}
