package com.khalid.vyntra.presentation.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.khalid.vyntra.domain.model.Invoice
import com.khalid.vyntra.domain.model.InvoiceStatus
import com.khalid.vyntra.presentation.components.Sparkline
import com.khalid.vyntra.presentation.navigation.Screen
import com.khalid.vyntra.presentation.theme.LossRed
import com.khalid.vyntra.presentation.theme.ProfitGreen
import com.khalid.vyntra.presentation.theme.StockCritical
import com.khalid.vyntra.presentation.theme.StockLow
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Dashboard — the landing screen.
 *
 * Layout (top → bottom):
 *  1. Greeting row: business-name eyebrow (when set), time-aware greeting,
 *     weekday + date. Settings icon inline on the right (no TopAppBar).
 *  2. Low-stock banner (only when count > 0).
 *  3. Hero revenue card: today's total + day-over-day delta chip +
 *     7-day sparkline.
 *  4. Secondary KPIs (2×2): transactions, avg sale, outstanding, products.
 *  5. Primary CTA: "New Sale" card.
 *  6. Three secondary actions: Add product, Scan, Purchase.
 *  7. Recent transactions list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val greeting = remember { greetingForNow() }
    val todayFormatted = remember {
        SimpleDateFormat("EEEE, d MMM", Locale.getDefault()).format(Date())
    }

    // Refresh on every resume. Without this the cached VM keeps showing
    // stale totals after the user creates a sale on NewSale and pops back —
    // init {} doesn't re-run because SharingStarted.WhileSubscribed keeps
    // the flow alive across the navigation.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    // Pull the status-bar inset directly. We don't wrap in a Scaffold (no
    // TopAppBar) so the LazyColumn pads itself.
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = topInset + 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GreetingHeader(
                businessName = uiState.businessName,
                greeting = greeting,
                dateLabel = todayFormatted,
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
            )
        }

        if (uiState.lowStockCount > 0) {
            item {
                LowStockBanner(
                    count = uiState.lowStockCount,
                    onClick = { navController.navigate(Screen.LowStockReport.route) }
                )
            }
        }

        item {
            HeroRevenueCard(
                todaySales = uiState.todaySales,
                deltaPercent = uiState.todayDeltaPercent,
                sparkline = uiState.last7DaysSales,
                currencySymbol = uiState.currencySymbol,
                onClick = { navController.navigate(Screen.SalesReport.route) }
            )
        }

        item {
            KpiPair(
                modifier = Modifier.padding(horizontal = 16.dp),
                left = "Transactions" to uiState.todayTransactionCount.toString(),
                right = "Avg sale" to formatMoney(uiState.todayAverageSale, uiState.currencySymbol)
            )
        }
        item {
            KpiPair(
                modifier = Modifier.padding(horizontal = 16.dp),
                left = "Outstanding" to formatMoney(uiState.totalOutstanding, uiState.currencySymbol),
                leftTint = if (uiState.totalOutstanding > 0) LossRed else null,
                right = "Products" to uiState.totalProducts.toString()
            )
        }

        item {
            QuickActionsRow(
                onNewSale = { navController.navigate(Screen.NewSale.route) },
                onAddProduct = { navController.navigate(Screen.AddEditProduct.createRoute()) },
                onScan = { navController.navigate(Screen.BarcodeScanner.route) },
                onPurchase = { navController.navigate(Screen.NewPurchase.route) }
            )
        }

        item {
            SectionHeader(
                title = "Recent transactions",
                actionLabel = "View all",
                onActionClick = { navController.navigate(Screen.InvoiceHistory.route) }
            )
        }
        if (uiState.recentInvoices.isEmpty()) {
            item { EmptyStateRow(message = "No transactions yet — start a sale.") }
        } else {
            items(items = uiState.recentInvoices, key = { it.id }) { invoice ->
                RecentInvoiceRow(
                    invoice = invoice,
                    currencySymbol = uiState.currencySymbol,
                    onClick = {
                        navController.navigate(Screen.InvoiceDetail.createRoute(invoice.id))
                    }
                )
            }
        }
    }
}

// ── Greeting ────────────────────────────────────────────────────────────────

@Composable
private fun GreetingHeader(
    businessName: String,
    greeting: String,
    dateLabel: String,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // Eyebrow only when a real business name is set — never falls
            // back to the brand name "Vyntra" since that's a redundant
            // header on the user's own dashboard.
            if (businessName.isNotBlank()) {
                Text(
                    text = businessName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(2.dp))
            }
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Low stock banner ────────────────────────────────────────────────────────

@Composable
private fun LowStockBanner(count: Int, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = StockLow.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.NotificationsActive,
                contentDescription = null,
                tint = StockLow,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "$count ${if (count == 1) "product" else "products"} low on stock",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Review",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ── Hero KPI card ───────────────────────────────────────────────────────────

@Composable
private fun HeroRevenueCard(
    todaySales: Double,
    deltaPercent: Double?,
    sparkline: List<Double>,
    currencySymbol: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "TODAY'S REVENUE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = formatMoney(todaySales, currencySymbol),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (deltaPercent != null) {
                    Spacer(Modifier.width(10.dp))
                    DeltaChip(deltaPercent, modifier = Modifier.padding(bottom = 8.dp))
                }
            }

            if (sparkline.size >= 2) {
                Spacer(Modifier.height(14.dp))
                Sparkline(
                    values = sparkline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    lineColor = MaterialTheme.colorScheme.primary,
                    fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Last 7 days",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DeltaChip(deltaPercent: Double, modifier: Modifier = Modifier) {
    val up = deltaPercent >= 0
    val color = if (up) ProfitGreen else LossRed
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = color.copy(alpha = 0.14f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (up) Icons.AutoMirrored.Filled.TrendingUp
                              else Icons.AutoMirrored.Filled.TrendingDown,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "${if (up) "+" else "-"}${String.format(Locale.getDefault(), "%.1f", abs(deltaPercent))}%",
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ── KPI row (two side-by-side metric chips) ─────────────────────────────────

@Composable
private fun KpiPair(
    left: Pair<String, String>,
    right: Pair<String, String>,
    modifier: Modifier = Modifier,
    leftTint: Color? = null,
    rightTint: Color? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricChipCard(Modifier.weight(1f), left.first, left.second, leftTint)
        MetricChipCard(Modifier.weight(1f), right.first, right.second, rightTint)
    }
}

@Composable
private fun MetricChipCard(
    modifier: Modifier,
    label: String,
    value: String,
    valueTint: Color?
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = valueTint ?: MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Quick actions ───────────────────────────────────────────────────────────

@Composable
private fun QuickActionsRow(
    onNewSale: () -> Unit,
    onAddProduct: () -> Unit,
    onScan: () -> Unit,
    onPurchase: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Hero CTA — the app's primary verb. Always present, always tinted.
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNewSale),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PointOfSale,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(10.dp).size(22.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "New Sale",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Start a checkout",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryAction(Modifier.weight(1f), "Add product", Icons.Filled.Add, onAddProduct)
            SecondaryAction(Modifier.weight(1f), "Scan", Icons.Filled.QrCodeScanner, onScan)
            SecondaryAction(Modifier.weight(1f), "Purchase", Icons.Filled.ShoppingCart, onPurchase)
        }
    }
}

@Composable
private fun SecondaryAction(
    modifier: Modifier,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

// ── Section header + recent invoice row + empty state ──────────────────────

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onActionClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (actionLabel != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun RecentInvoiceRow(
    invoice: Invoice,
    currencySymbol: String,
    onClick: () -> Unit
) {
    val statusColor = when (invoice.status) {
        InvoiceStatus.COMPLETED -> ProfitGreen
        InvoiceStatus.CREDIT -> StockLow
        InvoiceStatus.CANCELLED -> StockCritical
    }
    val dateLabel = remember(invoice.createdAt) {
        if (isSameDayAsToday(invoice.createdAt))
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(invoice.createdAt))
        else
            SimpleDateFormat("d MMM, hh:mm a", Locale.getDefault()).format(Date(invoice.createdAt))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = statusColor.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Receipt,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = invoice.invoiceNumber,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!invoice.customerName.isNullOrBlank()) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = invoice.customerName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Text(
                            text = "  •  ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = formatMoney(invoice.totalAmount, currencySymbol),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun EmptyStateRow(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Helpers ─────────────────────────────────────────────────────────────────

private fun greetingForNow(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Hello"
    }
}

/**
 * Format money using the user-chosen currency symbol from Settings, NOT
 * NumberFormat.getCurrencyInstance(locale) — the latter would render "$"
 * for many devices regardless of what's saved in DataStore.
 */
private fun formatMoney(amount: Double, currencySymbol: String): String {
    val number = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        maximumFractionDigits = 0
    }.format(amount)
    return "$currencySymbol $number"
}

private fun isSameDayAsToday(epoch: Long): Boolean {
    val today = Calendar.getInstance()
    val that = Calendar.getInstance().apply { timeInMillis = epoch }
    return today.get(Calendar.YEAR) == that.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == that.get(Calendar.DAY_OF_YEAR)
}
