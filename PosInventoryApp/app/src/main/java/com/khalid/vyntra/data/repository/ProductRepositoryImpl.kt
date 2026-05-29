package com.khalid.vyntra.data.repository

import com.khalid.vyntra.data.local.dao.CategoryDao
import com.khalid.vyntra.data.local.dao.ProductDao
import com.khalid.vyntra.data.local.dao.VendorDao
import com.khalid.vyntra.data.mapper.toDomain
import com.khalid.vyntra.data.mapper.toEntity
import com.khalid.vyntra.domain.model.Product
import com.khalid.vyntra.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao,
    private val vendorDao: VendorDao
) : ProductRepository {

    override fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAll().map { entities ->
            entities.map { entity ->
                val categoryName = entity.categoryId?.let { categoryDao.getById(it)?.name }
                val supplierName = entity.supplierId?.let { vendorDao.getById(it)?.name }
                entity.toDomain(
                    categoryName = categoryName,
                    supplierName = supplierName
                )
            }
        }
    }

    override suspend fun getProductById(id: Long): Product? {
        val entity = productDao.getById(id) ?: return null
        val categoryName = entity.categoryId?.let { categoryDao.getById(it)?.name }
        val supplierName = entity.supplierId?.let { vendorDao.getById(it)?.name }
        return entity.toDomain(
            categoryName = categoryName,
            supplierName = supplierName
        )
    }

    override fun searchProducts(query: String): Flow<List<Product>> {
        return productDao.searchByNameOrBarcode(query).map { entities ->
            entities.map { entity ->
                val categoryName = entity.categoryId?.let { categoryDao.getById(it)?.name }
                val supplierName = entity.supplierId?.let { vendorDao.getById(it)?.name }
                entity.toDomain(
                    categoryName = categoryName,
                    supplierName = supplierName
                )
            }
        }
    }

    override fun getProductsByCategory(categoryId: Long): Flow<List<Product>> {
        return productDao.getByCategory(categoryId).map { entities ->
            entities.map { entity ->
                val categoryName = entity.categoryId?.let { categoryDao.getById(it)?.name }
                val supplierName = entity.supplierId?.let { vendorDao.getById(it)?.name }
                entity.toDomain(
                    categoryName = categoryName,
                    supplierName = supplierName
                )
            }
        }
    }

    override fun getLowStockProducts(): Flow<List<Product>> {
        return productDao.getLowStockProducts().map { entities ->
            entities.map { entity ->
                val categoryName = entity.categoryId?.let { categoryDao.getById(it)?.name }
                val supplierName = entity.supplierId?.let { vendorDao.getById(it)?.name }
                entity.toDomain(
                    categoryName = categoryName,
                    supplierName = supplierName
                )
            }
        }
    }

    override suspend fun addProduct(product: Product): Long {
        return productDao.insert(product.toEntity())
    }

    override suspend fun updateProduct(product: Product) {
        productDao.update(product.toEntity())
    }

    override suspend fun deleteProduct(id: Long) {
        val entity = productDao.getById(id) ?: return
        productDao.delete(entity)
    }

    override suspend fun updateStock(productId: Long, newStock: Double) {
        productDao.updateStock(productId, newStock.toInt())
    }

    override suspend fun getProductCount(): Int {
        return productDao.getProductCount()
    }
}
