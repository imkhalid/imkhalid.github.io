package com.khalid.cashlytics.domain.repository

import com.khalid.cashlytics.domain.model.StockAdjustment
import kotlinx.coroutines.flow.Flow

interface StockAdjustmentRepository {
    fun getAll(): Flow<List<StockAdjustment>>
    fun getByProduct(productId: Long): Flow<List<StockAdjustment>>
    suspend fun addAdjustment(adjustment: StockAdjustment): Long
}
