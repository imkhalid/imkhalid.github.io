package com.khalid.vyntra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.khalid.vyntra.data.local.entity.InvoiceItemEntity

@Dao
interface InvoiceItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: InvoiceItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<InvoiceItemEntity>): List<Long>

    @Query("SELECT * FROM invoice_items WHERE invoice_id = :invoiceId")
    suspend fun getByInvoiceId(invoiceId: Long): List<InvoiceItemEntity>

    @Query("DELETE FROM invoice_items WHERE invoice_id = :invoiceId")
    suspend fun deleteByInvoiceId(invoiceId: Long)
}
