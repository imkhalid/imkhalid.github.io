package com.khalid.cashlytics.data.repository

import com.khalid.cashlytics.data.local.dao.CustomerDao
import com.khalid.cashlytics.data.local.dao.InvoiceDao
import com.khalid.cashlytics.data.local.dao.InvoiceItemDao
import com.khalid.cashlytics.data.mapper.toDomain
import com.khalid.cashlytics.data.mapper.toEntity
import com.khalid.cashlytics.domain.model.Invoice
import com.khalid.cashlytics.domain.model.InvoiceStatus
import com.khalid.cashlytics.domain.repository.InvoiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

class InvoiceRepositoryImpl @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val invoiceItemDao: InvoiceItemDao,
    private val customerDao: CustomerDao
) : InvoiceRepository {

    override fun getAll(): Flow<List<Invoice>> {
        return invoiceDao.getAll().map { entities ->
            entities.map { entity ->
                val customerName = entity.customerId?.let { customerDao.getById(it)?.name }
                entity.toDomain(customerName = customerName)
            }
        }
    }

    override suspend fun getById(id: Long): Invoice? {
        val invoiceWithItems = invoiceDao.getInvoiceWithItems(id) ?: return null
        val entity = invoiceWithItems.invoice
        val customerName = entity.customerId?.let { customerDao.getById(it)?.name }
        val items = invoiceWithItems.items.map { it.toDomain() }
        return entity.toDomain(customerName = customerName, items = items)
    }

    override fun getByCustomer(customerId: Long): Flow<List<Invoice>> {
        return invoiceDao.getByCustomer(customerId).map { entities ->
            entities.map { entity ->
                val customerName = entity.customerId?.let { customerDao.getById(it)?.name }
                entity.toDomain(customerName = customerName)
            }
        }
    }

    override fun getByDateRange(startDate: Long, endDate: Long): Flow<List<Invoice>> {
        return invoiceDao.getByDateRange(startDate, endDate).map { entities ->
            entities.map { entity ->
                val customerName = entity.customerId?.let { customerDao.getById(it)?.name }
                entity.toDomain(customerName = customerName)
            }
        }
    }

    override fun getByStatus(status: InvoiceStatus): Flow<List<Invoice>> {
        return invoiceDao.getByStatus(status.name).map { entities ->
            entities.map { entity ->
                val customerName = entity.customerId?.let { customerDao.getById(it)?.name }
                entity.toDomain(customerName = customerName)
            }
        }
    }

    override suspend fun createInvoice(invoice: Invoice): Long {
        val invoiceId = invoiceDao.insert(invoice.toEntity())
        if (invoice.items.isNotEmpty()) {
            val itemEntities = invoice.items.map { item ->
                item.toEntity().copy(invoiceId = invoiceId)
            }
            invoiceItemDao.insertAll(itemEntities)
        }
        return invoiceId
    }

    override suspend fun updateInvoice(invoice: Invoice) {
        invoiceDao.update(invoice.toEntity())
        invoiceItemDao.deleteByInvoiceId(invoice.id)
        if (invoice.items.isNotEmpty()) {
            val itemEntities = invoice.items.map { item ->
                item.toEntity().copy(invoiceId = invoice.id)
            }
            invoiceItemDao.insertAll(itemEntities)
        }
    }

    override suspend fun cancelInvoice(id: Long) {
        val entity = invoiceDao.getById(id) ?: return
        invoiceDao.update(
            entity.copy(
                status = InvoiceStatus.CANCELLED.name,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun getLastInvoiceNumber(): String? {
        return invoiceDao.getLastInvoiceNumber()
    }

    override suspend fun getDailySales(date: Long): Double {
        val calendar = Calendar.getInstance().apply { timeInMillis = date }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        return invoiceDao.getDailySales(startOfDay, endOfDay)
    }

    override suspend fun getMonthlySales(year: Int, month: Int): Double {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfMonth = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfMonth = calendar.timeInMillis

        return invoiceDao.getMonthlySales(startOfMonth, endOfMonth)
    }

    override fun getTopSellingProducts(
        limit: Int,
        startDate: Long,
        endDate: Long
    ): Flow<List<Pair<String, Int>>> {
        return invoiceDao.getAll().map {
            val results = invoiceDao.getTopSellingProducts(startDate, endDate, limit)
            results.map { product ->
                product.productName to product.totalQuantity
            }
        }
    }
}
