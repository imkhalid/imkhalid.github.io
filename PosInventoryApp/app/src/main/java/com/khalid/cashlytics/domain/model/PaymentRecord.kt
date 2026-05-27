package com.khalid.cashlytics.domain.model

data class PaymentRecord(
    val id: Long = 0,
    val entityType: EntityType,
    val entityId: Long,
    val entityName: String,
    val amount: Double,
    val paymentMethod: PaymentMethod,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
