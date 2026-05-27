package com.khalid.cashlytics.domain.model

data class InvoiceItem(
    val id: Long = 0,
    val invoiceId: Long = 0,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double,
    val discount: Double = 0.0,
    val totalPrice: Double
)
