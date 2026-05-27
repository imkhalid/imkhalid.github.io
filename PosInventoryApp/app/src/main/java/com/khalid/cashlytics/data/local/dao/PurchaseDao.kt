package com.khalid.cashlytics.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.khalid.cashlytics.data.local.entity.PurchaseEntity
import com.khalid.cashlytics.data.local.relation.PurchaseWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(purchase: PurchaseEntity): Long

    @Update
    suspend fun update(purchase: PurchaseEntity)

    @Delete
    suspend fun delete(purchase: PurchaseEntity)

    @Query("SELECT * FROM purchases ORDER BY created_at DESC")
    fun getAll(): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE id = :id")
    suspend fun getById(id: Long): PurchaseEntity?

    @Transaction
    @Query("SELECT * FROM purchases WHERE id = :id")
    suspend fun getPurchaseWithItems(id: Long): PurchaseWithItems?

    @Query("SELECT * FROM purchases WHERE vendor_id = :vendorId ORDER BY created_at DESC")
    fun getByVendor(vendorId: Long): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE created_at BETWEEN :startDate AND :endDate ORDER BY created_at DESC")
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<PurchaseEntity>>
}
