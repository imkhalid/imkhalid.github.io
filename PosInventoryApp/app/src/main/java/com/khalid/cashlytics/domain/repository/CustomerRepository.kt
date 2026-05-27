package com.khalid.cashlytics.domain.repository

import com.khalid.cashlytics.domain.model.Customer
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun getAll(): Flow<List<Customer>>
    suspend fun getById(id: Long): Customer?
    fun search(query: String): Flow<List<Customer>>
    suspend fun add(customer: Customer): Long
    suspend fun update(customer: Customer)
    suspend fun delete(id: Long)
    suspend fun updateOutstandingBalance(id: Long, amount: Double)
    fun getCustomersWithOutstanding(): Flow<List<Customer>>
}
