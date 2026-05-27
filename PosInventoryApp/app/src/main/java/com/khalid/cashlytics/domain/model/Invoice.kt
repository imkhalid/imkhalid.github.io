package com.khalid.cashlytics.domain.model

data class Invoice(
    val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long? = null,
    val customerName: String? = null,
    val items: List<InvoiceItem> = emptyList(),
    val subtotal: Double,
    val discountAmount: Double = 0.0,
    val taxPercent: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double,
    val paidAmount: Double,
    val changeAmount: Double = 0.0,
    val paymentMethod: PaymentMethod,
    val status: InvoiceStatus,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
