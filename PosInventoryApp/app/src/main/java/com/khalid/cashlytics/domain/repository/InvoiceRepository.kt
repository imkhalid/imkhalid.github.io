package com.khalid.cashlytics.domain.repository

import com.khalid.cashlytics.domain.model.Invoice
import com.khalid.cashlytics.domain.model.InvoiceStatus
import kotlinx.coroutines.flow.Flow

interface InvoiceRepository {
    fun getAll(): Flow<List<Invoice>>
    suspend fun getById(id: Long): Invoice?
    fun getByCustomer(customerId: Long): Flow<List<Invoice>>
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<Invoice>>
    fun getByStatus(status: InvoiceStatus): Flow<List<Invoice>>
    suspend fun createInvoice(invoice: Invoice): Long
    suspend fun updateInvoice(invoice: Invoice)
    suspend fun cancelInvoice(id: Long)
    suspend fun getLastInvoiceNumber(): String?
    suspend fun getDailySales(date: Long): Double
    suspend fun getMonthlySales(year: Int, month: Int): Double
    fun getTopSellingProducts(limit: Int, startDate: Long, endDate: Long): Flow<List<Pair<String, Int>>>
}
