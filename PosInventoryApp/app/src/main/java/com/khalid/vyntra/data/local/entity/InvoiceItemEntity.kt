package com.khalid.vyntra.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoice_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["invoice_id"]),
        Index(value = ["product_id"])
    ]
)
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "invoice_id")
    val invoiceId: Long,

    @ColumnInfo(name = "product_id")
    val productId: Long? = null,

    @ColumnInfo(name = "product_name")
    val productName: String,

    val quantity: Int,

    @ColumnInfo(name = "unit_price")
    val unitPrice: Double,

    val discount: Double = 0.0,

    @ColumnInfo(name = "total_price")
    val totalPrice: Double
)
