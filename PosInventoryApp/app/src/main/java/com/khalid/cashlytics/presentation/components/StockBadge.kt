package com.khalid.cashlytics.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.khalid.cashlytics.presentation.theme.BadgeShape
import com.khalid.cashlytics.presentation.theme.StockCritical
import com.khalid.cashlytics.presentation.theme.StockLow
import com.khalid.cashlytics.presentation.theme.StockOk
import com.khalid.cashlytics.presentation.theme.StockOutOfStock

enum class StockStatus {
    IN_STOCK,
    LOW_STOCK,
    CRITICAL,
    OUT_OF_STOCK
}

fun resolveStockStatus(currentStock: Double, minThreshold: Double): StockStatus {
    return when {
        currentStock <= 0 -> StockStatus.OUT_OF_STOCK
        currentStock <= minThreshold * 0.5 -> StockStatus.CRITICAL
        currentStock <= minThreshold -> StockStatus.LOW_STOCK
        else -> StockStatus.IN_STOCK
    }
}

@Composable
fun StockBadge(
    status: StockStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, label) = when (status) {
        StockStatus.IN_STOCK -> StockOk.copy(alpha = 0.15f) to "In Stock"
        StockStatus.LOW_STOCK -> StockLow.copy(alpha = 0.15f) to "Low Stock"
        StockStatus.CRITICAL -> StockCritical.copy(alpha = 0.15f) to "Critical"
        StockStatus.OUT_OF_STOCK -> StockOutOfStock.copy(alpha = 0.15f) to "Out of Stock"
    }

    val textColor: Color = when (status) {
        StockStatus.IN_STOCK -> StockOk
        StockStatus.LOW_STOCK -> StockLow
        StockStatus.CRITICAL -> StockCritical
        StockStatus.OUT_OF_STOCK -> StockOutOfStock
    }

    Text(
        text = label,
        modifier = modifier
            .clip(BadgeShape)
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        color = textColor
    )
}
