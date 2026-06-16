package com.khalid.vyntra.domain.usecase.inventory

import com.khalid.vyntra.domain.model.Product
import com.khalid.vyntra.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    operator fun invoke(): Flow<List<Product>> {
        return productRepository.getAllProducts()
    }

    fun byCategory(categoryId: Long): Flow<List<Product>> {
        return productRepository.getProductsByCategory(categoryId)
    }

    suspend fun byId(id: Long): Product? {
        return productRepository.getProductById(id)
    }
}
