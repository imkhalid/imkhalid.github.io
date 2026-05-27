package com.khalid.cashlytics.domain.model

data class Purchase(
    val id: Long = 0,
    val vendorId: Long,
    val vendorName: String,
    val items: List<PurchaseItem> = emptyList(),
    val totalAmount: Double,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
