package com.khalid.vyntra.domain.usecase.inventory

import com.khalid.vyntra.domain.model.Product
import com.khalid.vyntra.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLowStockProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    operator fun invoke(): Flow<List<Product>> {
        return productRepository.getLowStockProducts()
    }
}
