package com.khalid.cashlytics.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoices",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customer_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["customer_id"]),
        Index(value = ["invoice_number"], unique = true),
        Index(value = ["status"]),
        Index(value = ["created_at"])
    ]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "invoice_number")
    val invoiceNumber: String,

    @ColumnInfo(name = "customer_id")
    val customerId: Long? = null,

    val subtotal: Double = 0.0,

    @ColumnInfo(name = "discount_amount")
    val discountAmount: Double = 0.0,

    @ColumnInfo(name = "tax_percent")
    val taxPercent: Double = 0.0,

    @ColumnInfo(name = "tax_amount")
    val taxAmount: Double = 0.0,

    @ColumnInfo(name = "total_amount")
    val totalAmount: Double = 0.0,

    @ColumnInfo(name = "paid_amount")
    val paidAmount: Double = 0.0,

    @ColumnInfo(name = "change_amount")
    val changeAmount: Double = 0.0,

    @ColumnInfo(name = "payment_method")
    val paymentMethod: String = "CASH",

    val status: String = "COMPLETED",

    val notes: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
