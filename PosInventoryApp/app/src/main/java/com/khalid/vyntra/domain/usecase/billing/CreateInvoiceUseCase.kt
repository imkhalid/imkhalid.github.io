package com.khalid.vyntra.domain.usecase.billing

import com.khalid.vyntra.domain.model.AdjustmentType
import com.khalid.vyntra.domain.model.Invoice
import com.khalid.vyntra.domain.model.InvoiceStatus
import com.khalid.vyntra.domain.model.StockAdjustment
import com.khalid.vyntra.domain.repository.CustomerRepository
import com.khalid.vyntra.domain.repository.InvoiceRepository
import com.khalid.vyntra.domain.repository.ProductRepository
import com.khalid.vyntra.domain.repository.StockAdjustmentRepository
import javax.inject.Inject

class CreateInvoiceUseCase @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val stockAdjustmentRepository: StockAdjustmentRepository
) {
    suspend operator fun invoke(invoice: Invoice): Long {
        require(invoice.items.isNotEmpty()) { "Invoice must have at least one item" }
        require(invoice.totalAmount >= 0) { "Total amount cannot be negative" }

        // Validate stock availability and deduct stock for each item
        for (item in invoice.items) {
            val product = productRepository.getProductById(item.productId)
                ?: throw IllegalArgumentException("Product '${item.productName}' not found")

            if (product.currentStock < item.quantity) {
                throw IllegalStateException(
                    "Insufficient stock for '${product.name}'. Available: ${product.currentStock}, Requested: ${item.quantity}"
                )
            }

            val newStock = product.currentStock - item.quantity
            productRepository.updateStock(item.productId, newStock)

            stockAdjustmentRepository.addAdjustment(
                StockAdjustment(
                    productId = item.productId,
                    productName = item.productName,
                    previousStock = product.currentStock,
                    newStock = newStock,
                    adjustmentQuantity = -item.quantity,
                    reason = "Sale - Invoice ${invoice.invoiceNumber}",
                    type = AdjustmentType.SALE
                )
            )
        }

        // Update customer outstanding balance for credit sales
        if (invoice.status == InvoiceStatus.CREDIT && invoice.customerId != null) {
            val unpaidAmount = invoice.totalAmount - invoice.paidAmount
            if (unpaidAmount > 0) {
                customerRepository.updateOutstandingBalance(
                    invoice.customerId,
                    unpaidAmount
                )
            }
        }

        return invoiceRepository.createInvoice(invoice)
    }
}
