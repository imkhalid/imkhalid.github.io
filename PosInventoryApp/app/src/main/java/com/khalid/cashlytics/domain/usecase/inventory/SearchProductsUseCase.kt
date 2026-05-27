package com.khalid.cashlytics.domain.usecase.inventory

import com.khalid.cashlytics.domain.model.Product
import com.khalid.cashlytics.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    operator fun invoke(query: String): Flow<List<Product>> {
        return productRepository.searchProducts(query.trim())
    }
}
