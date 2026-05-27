package com.khalid.cashlytics.domain.model

data class StockAdjustment(
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val previousStock: Double,
    val newStock: Double,
    val adjustmentQuantity: Double,
    val reason: String?,
    val type: AdjustmentType,
    val createdAt: Long = System.currentTimeMillis()
)
