package com.khalid.cashlytics.domain.model

data class Customer(
    val id: Long = 0,
    val name: String,
    val phone: String?,
    val email: String? = null,
    val address: String? = null,
    val cnicOrId: String? = null,
    val creditLimit: Double = 0.0,
    val outstandingBalance: Double = 0.0,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
