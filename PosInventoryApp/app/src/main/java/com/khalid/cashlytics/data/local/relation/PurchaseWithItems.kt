package com.khalid.cashlytics.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.khalid.cashlytics.data.local.entity.PurchaseEntity
import com.khalid.cashlytics.data.local.entity.PurchaseItemEntity

data class PurchaseWithItems(
    @Embedded
    val purchase: PurchaseEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "purchase_id"
    )
    val items: List<PurchaseItemEntity>
)
