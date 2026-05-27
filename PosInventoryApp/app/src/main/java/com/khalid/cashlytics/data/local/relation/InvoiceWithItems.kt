package com.khalid.cashlytics.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.khalid.cashlytics.data.local.entity.InvoiceEntity
import com.khalid.cashlytics.data.local.entity.InvoiceItemEntity

data class InvoiceWithItems(
    @Embedded
    val invoice: InvoiceEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "invoice_id"
    )
    val items: List<InvoiceItemEntity>
)
