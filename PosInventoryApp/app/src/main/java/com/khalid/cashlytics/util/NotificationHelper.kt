package com.khalid.cashlytics.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.khalid.cashlytics.MainActivity
import com.khalid.cashlytics.domain.model.Product

object NotificationHelper {

    fun createNotificationChannels(context: Context) {
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_LOW_STOCK,
            "Low Stock Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for products below minimum stock threshold"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun showLowStockNotification(context: Context, products: List<Product>) {
        if (products.isEmpty()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "inventory")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val style = NotificationCompat.InboxStyle()
            .setBigContentTitle("${products.size} products below minimum stock")
        products.take(5).forEach { product ->
            style.addLine("${product.name}: ${product.currentStock} remaining (min: ${product.minStockThreshold})")
        }
        if (products.size > 5) {
            style.setSummaryText("+${products.size - 5} more")
        }

        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_LOW_STOCK)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Low Stock Alert")
            .setContentText("${products.size} products need restocking")
            .setStyle(style)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(1001, notification)
    }
}
