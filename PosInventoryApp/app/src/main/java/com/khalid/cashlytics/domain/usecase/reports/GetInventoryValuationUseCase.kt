package com.khalid.cashlytics.domain.usecase.reports

import com.khalid.cashlytics.domain.model.Product
import com.khalid.cashlytics.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class InventoryValuation(
    val products: List<ProductValuation>,
    val totalCostValue: Double,
    val totalRetailValue: Double,
    val potentialProfit: Double,
    val totalProductCount: Int,
    val totalStockUnits: Double
)

data class ProductValuation(
    val product: Product,
    val costValue: Double,
    val retailValue: Double,
    val potentialProfit: Double
)

class GetInventoryValuationUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(): InventoryValuation {
        val products = productRepository.getAllProducts().first()

        val valuations = products.map { product ->
            val costValue = product.currentStock * product.purchasePrice
            val retailValue = product.currentStock * product.sellingPrice
            ProductValuation(
                product = product,
                costValue = costValue,
                retailValue = retailValue,
                potentialProfit = retailValue - costValue
            )
        }

        return InventoryValuation(
            products = valuations,
            totalCostValue = valuations.sumOf { it.costValue },
            totalRetailValue = valuations.sumOf { it.retailValue },
            potentialProfit = valuations.sumOf { it.potentialProfit },
            totalProductCount = products.size,
            totalStockUnits = products.sumOf { it.currentStock }
        )
    }
}
