package com.khalid.cashlytics.domain.repository

import com.khalid.cashlytics.domain.model.Vendor
import kotlinx.coroutines.flow.Flow

interface VendorRepository {
    fun getAll(): Flow<List<Vendor>>
    suspend fun getById(id: Long): Vendor?
    fun search(query: String): Flow<List<Vendor>>
    suspend fun add(vendor: Vendor): Long
    suspend fun update(vendor: Vendor)
    suspend fun delete(id: Long)
    suspend fun updatePayableBalance(id: Long, amount: Double)
}
