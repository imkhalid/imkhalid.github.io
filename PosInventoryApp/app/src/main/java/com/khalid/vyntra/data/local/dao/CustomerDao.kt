package com.khalid.vyntra.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.khalid.vyntra.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: CustomerEntity): Long

    @Update
    suspend fun update(customer: CustomerEntity)

    @Delete
    suspend fun delete(customer: CustomerEntity)

    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAll(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getById(id: Long): CustomerEntity?

    @Query(
        """
        SELECT * FROM customers
        WHERE name LIKE '%' || :query || '%'
           OR phone LIKE '%' || :query || '%'
           OR email LIKE '%' || :query || '%'
        ORDER BY name ASC
        """
    )
    fun search(query: String): Flow<List<CustomerEntity>>

    @Query("UPDATE customers SET outstanding_balance = :balance WHERE id = :customerId")
    suspend fun updateOutstandingBalance(customerId: Long, balance: Double)

    @Query("SELECT * FROM customers WHERE outstanding_balance > 0 ORDER BY outstanding_balance DESC")
    fun getCustomersWithOutstanding(): Flow<List<CustomerEntity>>
}
