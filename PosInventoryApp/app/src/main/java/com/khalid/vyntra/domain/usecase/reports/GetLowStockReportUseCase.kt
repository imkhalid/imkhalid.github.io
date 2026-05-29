package com.khalid.vyntra.domain.usecase.reports

import com.khalid.vyntra.domain.model.Product
import com.khalid.vyntra.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class LowStockReport(
    val products: List<LowStockItem>,
    val totalLowStockCount: Int,
    val outOfStockCount: Int
)

data class LowStockItem(
    val product: Product,
    val deficit: Double,
    val isOutOfStock: Boolean
)

class GetLowStockReportUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    operator fun invoke(): Flow<LowStockReport> {
        return productRepository.getLowStockProducts().map { products ->
            val items = products.map { product ->
                LowStockItem(
                    product = product,
                    deficit = product.minStockThreshold - product.currentStock,
                    isOutOfStock = product.currentStock <= 0
                )
            }

            LowStockReport(
                products = items,
                totalLowStockCount = items.size,
                outOfStockCount = items.count { it.isOutOfStock }
            )
        }
    }
}
