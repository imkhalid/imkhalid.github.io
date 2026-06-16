package com.khalid.vyntra.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vendors")
data class VendorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val company: String = "",

    val phone: String = "",

    val email: String = "",

    val address: String = "",

    @ColumnInfo(name = "bank_details")
    val bankDetails: String = "",

    @ColumnInfo(name = "current_payable_balance")
    val currentPayableBalance: Double = 0.0,

    val notes: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
