package com.khalid.cashlytics.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payment_records",
    indices = [
        Index(value = ["entity_type", "entity_id"]),
        Index(value = ["created_at"])
    ]
)
data class PaymentRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "entity_type")
    val entityType: String,

    @ColumnInfo(name = "entity_id")
    val entityId: Long,

    val amount: Double,

    @ColumnInfo(name = "payment_method")
    val paymentMethod: String = "CASH",

    val notes: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
