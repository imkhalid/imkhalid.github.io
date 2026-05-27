package com.khalid.cashlytics.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.khalid.cashlytics.util.Constants
import java.util.concurrent.TimeUnit

/**
 * Schedules and cancels periodic background workers via [WorkManager].
 */
object WorkerScheduler {

    /**
     * Enqueues a periodic low-stock check that runs every 24 hours.
     * Uses [ExistingPeriodicWorkPolicy.KEEP] so re-calling this method
     * won't reset the timer if the work is already scheduled.
     */
    fun scheduleLowStockCheck(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<LowStockCheckWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .addTag(Constants.WORKER_LOW_STOCK)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            Constants.WORKER_LOW_STOCK,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    /**
     * Cancels the periodic low-stock check worker.
     */
    fun cancelLowStockCheck(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(Constants.WORKER_LOW_STOCK)
    }
}
