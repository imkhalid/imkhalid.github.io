package com.khalid.vyntra.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.khalid.vyntra.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity): Long

    @Update
    suspend fun update(product: ProductEntity)

    @Delete
    suspend fun delete(product: ProductEntity)

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: Long): ProductEntity?

    @Query(
        """
        SELECT * FROM products
        WHERE name LIKE '%' || :query || '%'
           OR barcode LIKE '%' || :query || '%'
           OR sku LIKE '%' || :query || '%'
        ORDER BY name ASC
        """
    )
    fun searchByNameOrBarcode(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE category_id = :categoryId ORDER BY name ASC")
    fun getByCategory(categoryId: Long): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE current_stock <= min_stock_threshold ORDER BY current_stock ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Query("UPDATE products SET current_stock = :newStock, updated_at = :updatedAt WHERE id = :productId")
    suspend fun updateStock(productId: Long, newStock: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int
}
