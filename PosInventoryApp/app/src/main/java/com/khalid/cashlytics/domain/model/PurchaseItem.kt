package com.khalid.cashlytics.domain.model

data class PurchaseItem(
    val id: Long = 0,
    val purchaseId: Long = 0,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val costPrice: Double,
    val totalPrice: Double
)
