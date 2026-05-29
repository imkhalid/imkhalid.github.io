package com.khalid.vyntra.domain.repository

import com.khalid.vyntra.domain.model.StockAdjustment
import kotlinx.coroutines.flow.Flow

interface StockAdjustmentRepository {
    fun getAll(): Flow<List<StockAdjustment>>
    fun getByProduct(productId: Long): Flow<List<StockAdjustment>>
    suspend fun addAdjustment(adjustment: StockAdjustment): Long
}
