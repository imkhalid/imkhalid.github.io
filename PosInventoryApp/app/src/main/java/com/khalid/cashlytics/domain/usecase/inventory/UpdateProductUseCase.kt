package com.khalid.cashlytics.domain.usecase.inventory

import com.khalid.cashlytics.domain.model.Product
import com.khalid.cashlytics.domain.repository.ProductRepository
import javax.inject.Inject

class UpdateProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(product: Product) {
        require(product.id > 0) { "Product ID must be valid" }
        require(product.name.isNotBlank()) { "Product name cannot be empty" }
        require(product.sellingPrice >= 0) { "Selling price cannot be negative" }
        require(product.purchasePrice >= 0) { "Purchase price cannot be negative" }
        productRepository.updateProduct(
            product.copy(updatedAt = System.currentTimeMillis())
        )
    }
}
