package com.khalid.cashlytics.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.khalid.cashlytics.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

data class CategoryWithProductCount(
    val id: Long,
    val name: String,
    val description: String,
    val createdAt: Long,
    val productCount: Int
)

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): CategoryEntity?

    @Query(
        """
        SELECT c.id, c.name, c.description, c.created_at AS createdAt,
               COUNT(p.id) AS productCount
        FROM categories c
        LEFT JOIN products p ON p.category_id = c.id
        GROUP BY c.id
        ORDER BY c.name ASC
        """
    )
    fun getCategoryWithProductCount(): Flow<List<CategoryWithProductCount>>
}
