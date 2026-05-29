package com.khalid.vyntra.domain.usecase.reports

import com.khalid.vyntra.domain.model.InvoiceStatus
import com.khalid.vyntra.domain.repository.InvoiceRepository
import com.khalid.vyntra.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class ProfitLossReport(
    val totalRevenue: Double,
    val totalCostOfGoods: Double,
    val grossProfit: Double,
    val totalPurchases: Double,
    val netProfit: Double,
    val invoiceCount: Int,
    val cancelledCount: Int
)

class GetProfitLossUseCase @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val purchaseRepository: PurchaseRepository
) {
    suspend operator fun invoke(startDate: Long, endDate: Long): ProfitLossReport {
        val invoices = invoiceRepository.getByDateRange(startDate, endDate).first()
        val purchases = purchaseRepository.getByDateRange(startDate, endDate).first()

        val activeInvoices = invoices.filter { it.status != InvoiceStatus.CANCELLED }
        val cancelledInvoices = invoices.filter { it.status == InvoiceStatus.CANCELLED }

        val totalRevenue = activeInvoices.sumOf { it.totalAmount }

        val totalCostOfGoods = activeInvoices.sumOf { invoice ->
            invoice.items.sumOf { item -> item.quantity * item.unitPrice }
        }

        val totalPurchases = purchases.sumOf { it.totalAmount }

        val grossProfit = totalRevenue - totalCostOfGoods
        val netProfit = totalRevenue - totalPurchases

        return ProfitLossReport(
            totalRevenue = totalRevenue,
            totalCostOfGoods = totalCostOfGoods,
            grossProfit = grossProfit,
            totalPurchases = totalPurchases,
            netProfit = netProfit,
            invoiceCount = activeInvoices.size,
            cancelledCount = cancelledInvoices.size
        )
    }
}
