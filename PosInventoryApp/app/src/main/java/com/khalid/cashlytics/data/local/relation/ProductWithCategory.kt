package com.khalid.cashlytics.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.khalid.cashlytics.data.local.entity.CategoryEntity
import com.khalid.cashlytics.data.local.entity.ProductEntity

data class ProductWithCategory(
    @Embedded
    val product: ProductEntity,

    @Relation(
        parentColumn = "category_id",
        entityColumn = "id"
    )
    val category: CategoryEntity?
)
