package com.khalid.cashlytics.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.khalid.cashlytics.data.local.dao.ProductDao
import com.khalid.cashlytics.data.mapper.toDomain
import com.khalid.cashlytics.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Periodic background worker that checks for products whose stock
 * has fallen below the minimum threshold and fires a notification.
 */
@HiltWorker
class LowStockCheckWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val productDao: ProductDao
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "LowStockCheckWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            val lowStockEntities = productDao.getLowStockProducts().first()
            if (lowStockEntities.isNotEmpty()) {
                val products = lowStockEntities.map { it.toDomain() }
                NotificationHelper.showLowStockNotification(applicationContext, products)
                Log.d(TAG, "Low stock notification shown for ${products.size} products")
            } else {
                Log.d(TAG, "No low stock products found")
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking low stock products", e)
            Result.success() // Don't retry; will run again next period
        }
    }
}
