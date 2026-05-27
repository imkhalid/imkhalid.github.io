package com.khalid.cashlytics.domain.usecase.purchase

import com.khalid.cashlytics.domain.model.AdjustmentType
import com.khalid.cashlytics.domain.model.Purchase
import com.khalid.cashlytics.domain.model.StockAdjustment
import com.khalid.cashlytics.domain.repository.ProductRepository
import com.khalid.cashlytics.domain.repository.PurchaseRepository
import com.khalid.cashlytics.domain.repository.StockAdjustmentRepository
import com.khalid.cashlytics.domain.repository.VendorRepository
import javax.inject.Inject

class CreatePurchaseUseCase @Inject constructor(
    private val purchaseRepository: PurchaseRepository,
    private val productRepository: ProductRepository,
    private val vendorRepository: VendorRepository,
    private val stockAdjustmentRepository: StockAdjustmentRepository
) {
    suspend operator fun invoke(purchase: Purchase): Long {
        require(purchase.items.isNotEmpty()) { "Purchase must have at least one item" }
        require(purchase.vendorId > 0) { "Vendor must be specified" }
        require(purchase.totalAmount >= 0) { "Total amount cannot be negative" }

        // Add stock for each purchased item
        for (item in purchase.items) {
            val product = productRepository.getProductById(item.productId)
                ?: throw IllegalArgumentException("Product '${item.productName}' not found")

            val newStock = product.currentStock + item.quantity
            productRepository.updateStock(item.productId, newStock)

            stockAdjustmentRepository.addAdjustment(
                StockAdjustment(
                    productId = item.productId,
                    productName = item.productName,
                    previousStock = product.currentStock,
                    newStock = newStock,
                    adjustmentQuantity = item.quantity,
                    reason = "Purchase from ${purchase.vendorName}",
                    type = AdjustmentType.PURCHASE
                )
            )
        }

        // Update vendor payable balance
        vendorRepository.updatePayableBalance(purchase.vendorId, purchase.totalAmount)

        return purchaseRepository.createPurchase(purchase)
    }
}
