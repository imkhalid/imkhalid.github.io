package com.khalid.cashlytics.domain.model

data class Vendor(
    val id: Long = 0,
    val name: String,
    val company: String?,
    val phone: String?,
    val email: String? = null,
    val address: String? = null,
    val bankDetails: String? = null,
    val currentPayableBalance: Double = 0.0,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
