package com.khalid.cashlytics.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.khalid.cashlytics.data.local.entity.PurchaseItemEntity

@Dao
interface PurchaseItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PurchaseItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PurchaseItemEntity>): List<Long>

    @Query("SELECT * FROM purchase_items WHERE purchase_id = :purchaseId")
    suspend fun getByPurchaseId(purchaseId: Long): List<PurchaseItemEntity>
}
