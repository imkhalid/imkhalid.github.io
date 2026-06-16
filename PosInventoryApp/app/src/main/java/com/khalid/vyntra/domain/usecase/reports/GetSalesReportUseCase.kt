package com.khalid.vyntra.domain.usecase.reports

import com.khalid.vyntra.domain.model.Invoice
import com.khalid.vyntra.domain.repository.InvoiceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class SalesReport(
    val invoices: Flow<List<Invoice>>,
    val dailySales: Double,
    val monthlySales: Double,
    val topSellingProducts: Flow<List<Pair<String, Int>>>
)

class GetSalesReportUseCase @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) {
    suspend operator fun invoke(
        startDate: Long,
        endDate: Long,
        currentDate: Long,
        year: Int,
        month: Int,
        topProductsLimit: Int = 10
    ): SalesReport {
        return SalesReport(
            invoices = invoiceRepository.getByDateRange(startDate, endDate),
            dailySales = invoiceRepository.getDailySales(currentDate),
            monthlySales = invoiceRepository.getMonthlySales(year, month),
            topSellingProducts = invoiceRepository.getTopSellingProducts(
                topProductsLimit, startDate, endDate
            )
        )
    }
}
