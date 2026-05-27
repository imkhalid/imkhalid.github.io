package com.khalid.cashlytics.domain.usecase.billing

import com.khalid.cashlytics.domain.model.AdjustmentType
import com.khalid.cashlytics.domain.model.InvoiceStatus
import com.khalid.cashlytics.domain.model.StockAdjustment
import com.khalid.cashlytics.domain.repository.CustomerRepository
import com.khalid.cashlytics.domain.repository.InvoiceRepository
import com.khalid.cashlytics.domain.repository.ProductRepository
import com.khalid.cashlytics.domain.repository.StockAdjustmentRepository
import javax.inject.Inject

class CancelInvoiceUseCase @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val stockAdjustmentRepository: StockAdjustmentRepository
) {
    suspend operator fun invoke(invoiceId: Long) {
        require(invoiceId > 0) { "Invoice ID must be valid" }

        val invoice = invoiceRepository.getById(invoiceId)
            ?: throw IllegalArgumentException("Invoice not found")

        if (invoice.status == InvoiceStatus.CANCELLED) {
            throw IllegalStateException("Invoice is already cancelled")
        }

        // Reverse stock for each item
        for (item in invoice.items) {
            val product = productRepository.getProductById(item.productId)
            if (product != null) {
                val restoredStock = product.currentStock + item.quantity
                productRepository.updateStock(item.productId, restoredStock)

                stockAdjustmentRepository.addAdjustment(
                    StockAdjustment(
                        productId = item.productId,
                        productName = item.productName,
                        previousStock = product.currentStock,
                        newStock = restoredStock,
                        adjustmentQuantity = item.quantity,
                        reason = "Cancelled - Invoice ${invoice.invoiceNumber}",
                        type = AdjustmentType.RETURN
                    )
                )
            }
        }

        // Reverse customer outstanding balance if it was a credit sale
        if (invoice.status == InvoiceStatus.CREDIT && invoice.customerId != null) {
            val unpaidAmount = invoice.totalAmount - invoice.paidAmount
            if (unpaidAmount > 0) {
                customerRepository.updateOutstandingBalance(
                    invoice.customerId,
                    -unpaidAmount
                )
            }
        }

        invoiceRepository.cancelInvoice(invoiceId)
    }
}
