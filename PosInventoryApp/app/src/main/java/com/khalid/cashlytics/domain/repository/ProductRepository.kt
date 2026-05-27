package com.khalid.cashlytics.domain.repository

import com.khalid.cashlytics.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getAllProducts(): Flow<List<Product>>
    suspend fun getProductById(id: Long): Product?
    fun searchProducts(query: String): Flow<List<Product>>
    fun getProductsByCategory(categoryId: Long): Flow<List<Product>>
    fun getLowStockProducts(): Flow<List<Product>>
    suspend fun addProduct(product: Product): Long
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(id: Long)
    suspend fun updateStock(productId: Long, newStock: Double)
    suspend fun getProductCount(): Int
}
