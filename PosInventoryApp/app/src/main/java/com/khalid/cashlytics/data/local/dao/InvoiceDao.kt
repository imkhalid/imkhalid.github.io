package com.khalid.cashlytics.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.khalid.cashlytics.data.local.entity.InvoiceEntity
import com.khalid.cashlytics.data.local.relation.InvoiceWithItems
import kotlinx.coroutines.flow.Flow

data class TopSellingProduct(
    val productId: Long,
    val productName: String,
    val totalQuantity: Int,
    val totalRevenue: Double
)

@Dao
interface InvoiceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invoice: InvoiceEntity): Long

    @Update
    suspend fun update(invoice: InvoiceEntity)

    @Delete
    suspend fun delete(invoice: InvoiceEntity)

    @Query("SELECT * FROM invoices ORDER BY created_at DESC")
    fun getAll(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getById(id: Long): InvoiceEntity?

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceWithItems(id: Long): InvoiceWithItems?

    @Query("SELECT * FROM invoices WHERE customer_id = :customerId ORDER BY created_at DESC")
    fun getByCustomer(customerId: Long): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE created_at BETWEEN :startDate AND :endDate ORDER BY created_at DESC")
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE status = :status ORDER BY created_at DESC")
    fun getByStatus(status: String): Flow<List<InvoiceEntity>>

    @Query("SELECT invoice_number FROM invoices ORDER BY id DESC LIMIT 1")
    suspend fun getLastInvoiceNumber(): String?

    @Query(
        """
        SELECT COALESCE(SUM(total_amount), 0) FROM invoices
        WHERE status != 'CANCELLED'
          AND created_at BETWEEN :startOfDay AND :endOfDay
        """
    )
    suspend fun getDailySales(startOfDay: Long, endOfDay: Long): Double

    @Query(
        """
        SELECT COALESCE(SUM(total_amount), 0) FROM invoices
        WHERE status != 'CANCELLED'
          AND created_at BETWEEN :startOfMonth AND :endOfMonth
        """
    )
    suspend fun getMonthlySales(startOfMonth: Long, endOfMonth: Long): Double

    @Query(
        """
        SELECT ii.product_id AS productId,
               ii.product_name AS productName,
               SUM(ii.quantity) AS totalQuantity,
               SUM(ii.total_price) AS totalRevenue
        FROM invoice_items ii
        INNER JOIN invoices i ON i.id = ii.invoice_id
        WHERE i.status != 'CANCELLED'
          AND i.created_at BETWEEN :startDate AND :endDate
        GROUP BY ii.product_id, ii.product_name
        ORDER BY totalQuantity DESC
        LIMIT :limit
        """
    )
    suspend fun getTopSellingProducts(startDate: Long, endDate: Long, limit: Int = 10): List<TopSellingProduct>
}
