package com.khalid.vyntra.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val productCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
