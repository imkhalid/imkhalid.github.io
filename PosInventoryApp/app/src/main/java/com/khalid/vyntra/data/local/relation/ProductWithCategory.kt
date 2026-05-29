package com.khalid.vyntra.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.khalid.vyntra.data.local.entity.CategoryEntity
import com.khalid.vyntra.data.local.entity.ProductEntity

data class ProductWithCategory(
    @Embedded
    val product: ProductEntity,

    @Relation(
        parentColumn = "category_id",
        entityColumn = "id"
    )
    val category: CategoryEntity?
)
