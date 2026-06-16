package com.khalid.vyntra.domain.usecase.inventory

import com.khalid.vyntra.domain.repository.ProductRepository
import javax.inject.Inject

class DeleteProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(productId: Long) {
        require(productId > 0) { "Product ID must be valid" }
        productRepository.deleteProduct(productId)
    }
}
