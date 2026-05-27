package com.khalid.cashlytics.domain.usecase.billing

import com.khalid.cashlytics.domain.model.Invoice
import com.khalid.cashlytics.domain.model.InvoiceStatus
import com.khalid.cashlytics.domain.repository.InvoiceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetInvoicesUseCase @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) {
    operator fun invoke(): Flow<List<Invoice>> {
        return invoiceRepository.getAll()
    }

    fun byCustomer(customerId: Long): Flow<List<Invoice>> {
        return invoiceRepository.getByCustomer(customerId)
    }

    fun byDateRange(startDate: Long, endDate: Long): Flow<List<Invoice>> {
        return invoiceRepository.getByDateRange(startDate, endDate)
    }

    fun byStatus(status: InvoiceStatus): Flow<List<Invoice>> {
        return invoiceRepository.getByStatus(status)
    }

    suspend fun byId(id: Long): Invoice? {
        return invoiceRepository.getById(id)
    }
}
