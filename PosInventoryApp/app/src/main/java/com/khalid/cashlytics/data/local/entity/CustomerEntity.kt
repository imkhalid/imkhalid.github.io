package com.khalid.cashlytics.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val phone: String = "",

    val email: String = "",

    val address: String = "",

    @ColumnInfo(name = "cnic_or_id")
    val cnicOrId: String = "",

    @ColumnInfo(name = "credit_limit")
    val creditLimit: Double = 0.0,

    @ColumnInfo(name = "outstanding_balance")
    val outstandingBalance: Double = 0.0,

    val notes: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
