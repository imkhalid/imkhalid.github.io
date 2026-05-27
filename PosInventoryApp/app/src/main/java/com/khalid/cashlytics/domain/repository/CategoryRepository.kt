package com.khalid.cashlytics.domain.repository

import com.khalid.cashlytics.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAll(): Flow<List<Category>>
    suspend fun getById(id: Long): Category?
    suspend fun add(category: Category): Long
    suspend fun update(category: Category)
    suspend fun delete(id: Long)
}
