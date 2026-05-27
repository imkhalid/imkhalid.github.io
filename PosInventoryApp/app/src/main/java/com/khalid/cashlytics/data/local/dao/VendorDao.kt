package com.khalid.cashlytics.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.khalid.cashlytics.data.local.entity.VendorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VendorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vendor: VendorEntity): Long

    @Update
    suspend fun update(vendor: VendorEntity)

    @Delete
    suspend fun delete(vendor: VendorEntity)

    @Query("SELECT * FROM vendors ORDER BY name ASC")
    fun getAll(): Flow<List<VendorEntity>>

    @Query("SELECT * FROM vendors WHERE id = :id")
    suspend fun getById(id: Long): VendorEntity?

    @Query(
        """
        SELECT * FROM vendors
        WHERE name LIKE '%' || :query || '%'
           OR company LIKE '%' || :query || '%'
           OR phone LIKE '%' || :query || '%'
        ORDER BY name ASC
        """
    )
    fun search(query: String): Flow<List<VendorEntity>>

    @Query("UPDATE vendors SET current_payable_balance = :balance WHERE id = :vendorId")
    suspend fun updatePayableBalance(vendorId: Long, balance: Double)
}
