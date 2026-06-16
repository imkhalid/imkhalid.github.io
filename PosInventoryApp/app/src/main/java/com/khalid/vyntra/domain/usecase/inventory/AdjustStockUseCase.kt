package com.khalid.vyntra.domain.usecase.inventory

import com.khalid.vyntra.domain.model.AdjustmentType
import com.khalid.vyntra.domain.model.StockAdjustment
import com.khalid.vyntra.domain.repository.ProductRepository
import com.khalid.vyntra.domain.repository.StockAdjustmentRepository
import javax.inject.Inject

class AdjustStockUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val stockAdjustmentRepository: StockAdjustmentRepository
) {
    suspend operator fun invoke(
        productId: Long,
        newStock: Double,
        reason: String?,
        type: AdjustmentType
    ): Long {
        require(productId > 0) { "Product ID must be valid" }
        require(newStock >= 0) { "Stock cannot be negative" }

        val product = productRepository.getProductById(productId)
            ?: throw IllegalArgumentException("Product not found")

        val adjustment = StockAdjustment(
            productId = productId,
            productName = product.name,
            previousStock = product.currentStock,
            newStock = newStock,
            adjustmentQuantity = newStock - product.currentStock,
            reason = reason,
            type = type
        )

        productRepository.updateStock(productId, newStock)
        return stockAdjustmentRepository.addAdjustment(adjustment)
    }
}
