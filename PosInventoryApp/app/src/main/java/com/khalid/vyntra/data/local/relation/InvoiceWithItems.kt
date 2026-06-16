package com.khalid.vyntra.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.khalid.vyntra.data.local.entity.InvoiceEntity
import com.khalid.vyntra.data.local.entity.InvoiceItemEntity

data class InvoiceWithItems(
    @Embedded
    val invoice: InvoiceEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "invoice_id"
    )
    val items: List<InvoiceItemEntity>
)
