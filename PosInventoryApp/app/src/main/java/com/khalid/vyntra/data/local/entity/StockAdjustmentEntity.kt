package com.khalid.vyntra.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_adjustments",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["product_id"]),
        Index(value = ["created_at"])
    ]
)
data class StockAdjustmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "product_id")
    val productId: Long,

    @ColumnInfo(name = "previous_stock")
    val previousStock: Int,

    @ColumnInfo(name = "new_stock")
    val newStock: Int,

    @ColumnInfo(name = "adjustment_quantity")
    val adjustmentQuantity: Int,

    val reason: String = "",

    val type: String = "MANUAL",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
