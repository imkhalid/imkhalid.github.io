package com.khalid.cashlytics.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.khalid.cashlytics.data.local.entity.PaymentRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: PaymentRecordEntity): Long

    @Query(
        """
        SELECT * FROM payment_records
        WHERE entity_type = :entityType AND entity_id = :entityId
        ORDER BY created_at DESC
        """
    )
    fun getByEntity(entityType: String, entityId: Long): Flow<List<PaymentRecordEntity>>

    @Query("SELECT * FROM payment_records ORDER BY created_at DESC")
    fun getAll(): Flow<List<PaymentRecordEntity>>
}
