package com.khalid.vyntra.domain.usecase.inventory

import com.khalid.vyntra.domain.model.Product
import com.khalid.vyntra.domain.repository.ProductRepository
import javax.inject.Inject

class AddProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(product: Product): Long {
        require(product.name.isNotBlank()) { "Product name cannot be empty" }
        require(product.sellingPrice >= 0) { "Selling price cannot be negative" }
        require(product.purchasePrice >= 0) { "Purchase price cannot be negative" }
        require(product.currentStock >= 0) { "Stock cannot be negative" }
        return productRepository.addProduct(product)
    }
}
