package com.khalid.cashlytics.domain.model

data class Product(
    val id: Long = 0,
    val name: String,
    val sku: String,
    val barcode: String?,
    val categoryId: Long?,
    val categoryName: String? = null,
    val unit: String,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val currentStock: Double,
    val minStockThreshold: Double,
    val supplierId: Long?,
    val supplierName: String? = null,
    val imagePath: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
