package com.khalid.vyntra.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.khalid.vyntra.data.local.entity.PurchaseEntity
import com.khalid.vyntra.data.local.entity.PurchaseItemEntity

data class PurchaseWithItems(
    @Embedded
    val purchase: PurchaseEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "purchase_id"
    )
    val items: List<PurchaseItemEntity>
)
