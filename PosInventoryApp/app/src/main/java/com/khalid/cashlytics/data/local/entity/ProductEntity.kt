package com.khalid.cashlytics.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = VendorEntity::class,
            parentColumns = ["id"],
            childColumns = ["supplier_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["category_id"]),
        Index(value = ["supplier_id"]),
        Index(value = ["sku"]),
        Index(value = ["barcode"])
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val sku: String = "",

    val barcode: String = "",

    @ColumnInfo(name = "category_id")
    val categoryId: Long? = null,

    val unit: String = "",

    @ColumnInfo(name = "purchase_price")
    val purchasePrice: Double = 0.0,

    @ColumnInfo(name = "selling_price")
    val sellingPrice: Double = 0.0,

    @ColumnInfo(name = "current_stock")
    val currentStock: Int = 0,

    @ColumnInfo(name = "min_stock_threshold")
    val minStockThreshold: Int = 0,

    @ColumnInfo(name = "supplier_id")
    val supplierId: Long? = null,

    @ColumnInfo(name = "image_path")
    val imagePath: String? = null,

    val notes: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
