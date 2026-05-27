package com.khalid.cashlytics.presentation.dashboard

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.khalid.cashlytics.domain.model.Invoice
import com.khalid.cashlytics.domain.model.InvoiceStatus
import com.khalid.cashlytics.domain.model.Product
import com.khalid.cashlytics.presentation.navigation.Screen
import com.khalid.cashlytics.presentation.theme.ProfitGreen
import com.khalid.cashlytics.presentation.theme.StockCritical
import com.khalid.cashlytics.presentation.theme.StockLow
import com.khalid.cashlytics.presentation.theme.StatCardShape
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val todayFormatted = remember {
        SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()).format(Date())
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            DashboardTopBar(
                scrollBehavior = scrollBehavior,
                todayFormatted = todayFormatted,
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Stat Cards - Horizontal Scrollable Row
                item {
                    StatCardsRow(uiState = uiState)
                }

                // Quick Actions
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    QuickActionsSection(navController = navController)
                }

                // Recent Transactions Header
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader(
                        title = "Recent Transactions",
                        icon = Icons.Filled.Receipt,
                        actionText = "View All",
                        onActionClick = { navController.navigate(Screen.InvoiceHistory.route) }
                    )
                }

                // Recent Transactions List
                if (uiState.recentInvoices.isEmpty()) {
                    item {
                        EmptyStateCard(
                            message = "No transactions yet. Create your first sale!",
                            icon = Icons.Filled.PointOfSale
                        )
                    }
                } else {
                    items(
                        items = uiState.recentInvoices,
                        key = { it.id }
                    ) { invoice ->
                        RecentInvoiceItem(
                            invoice = invoice,
                            onClick = {
                                navController.navigate(
                                    Screen.InvoiceDetail.createRoute(invoice.id)
                                )
                            }
                        )
                    }
                }

                // Low Stock Alerts Header
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader(
                        title = "Low Stock Alerts",
                        icon = Icons.Filled.Warning,
                        iconTint = StockLow,
                        actionText = if (uiState.lowStockProducts.isNotEmpty()) "View All" else null,
                        onActionClick = { navController.navigate(Screen.LowStockReport.route) }
                    )
                }

                // Low Stock Products
                if (uiState.lowStockProducts.isEmpty()) {
                    item {
                        EmptyStateCard(
                            message = "All products are well stocked.",
                            icon = Icons.Outlined.Inventory2
                        )
                    }
                } else {
                    items(
                        items = uiState.lowStockProducts,
                        key = { it.id }
                    ) { product ->
                        LowStockItem(
                            product = product,
                            onClick = {
                                navController.navigate(
                                    Screen.AddEditProduct.createRoute(product.id)
                                )
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    todayFormatted: String,
    onSettingsClick: () -> Unit
) {
    LargeTopAppBar(
        title = {
            Column {
                Text(
                    text = "Cashlytics",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = todayFormatted,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            IconButton(onClick = { /* Notifications */ }) {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = "Notifications"
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = "Settings"
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    )
}

// ── Stat Cards ──────────────────────────────────────────────────────────────

@Composable
private fun StatCardsRow(uiState: DashboardUiState) {
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
            maximumFractionDigits = 0
        }
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            StatCard(
                title = "Today's Sales",
                value = currencyFormatter.format(uiState.todaySales),
                icon = Icons.Filled.PointOfSale,
                gradientColors = listOf(
                    Color(0xFF1565C0),
                    Color(0xFF1E88E5)
                )
            )
        }
        item {
            StatCard(
                title = "Monthly Sales",
                value = currencyFormatter.format(uiState.monthlySales),
                icon = Icons.Filled.TrendingUp,
                gradientColors = listOf(
                    Color(0xFF00897B),
                    Color(0xFF26A69A)
                )
            )
        }
        item {
            StatCard(
                title = "Total Products",
                value = uiState.totalProducts.toString(),
                icon = Icons.Filled.Inventory,
                gradientColors = listOf(
                    Color(0xFF5E35B1),
                    Color(0xFF7E57C2)
                )
            )
        }
        item {
            StatCard(
                title = "Low Stock",
                value = uiState.lowStockCount.toString(),
                icon = Icons.Filled.Warning,
                gradientColors = if (uiState.lowStockCount > 0) {
                    listOf(Color(0xFFE65100), Color(0xFFFF8F00))
                } else {
                    listOf(Color(0xFF2E7D32), Color(0xFF43A047))
                }
            )
        }
        item {
            StatCard(
                title = "Outstanding",
                value = currencyFormatter.format(uiState.totalOutstanding),
                icon = Icons.Filled.AttachMoney,
                gradientColors = if (uiState.totalOutstanding > 0) {
                    listOf(Color(0xFFC62828), Color(0xFFE53935))
                } else {
                    listOf(Color(0xFF2E7D32), Color(0xFF43A047))
                }
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    gradientColors: List<Color>
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(120.dp),
        shape = StatCardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(colors = gradientColors)
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

// ── Quick Actions ───────────────────────────────────────────────────────────

@Composable
private fun QuickActionsSection(navController: NavController) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                label = "New Sale",
                icon = Icons.Filled.PointOfSale,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = { navController.navigate(Screen.NewSale.route) }
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                label = "Add Product",
                icon = Icons.Filled.Add,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = { navController.navigate(Screen.AddEditProduct.createRoute()) }
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                label = "Purchase",
                icon = Icons.Filled.ShoppingCart,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = { navController.navigate(Screen.NewPurchase.route) }
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(88.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

// ── Section Header ──────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    actionText: String? = null,
    onActionClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (actionText != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

// ── Recent Invoice Item ─────────────────────────────────────────────────────

@Composable
private fun RecentInvoiceItem(
    invoice: Invoice,
    onClick: () -> Unit
) {
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale.getDefault())
    }
    val dateFormatter = remember {
        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    }

    val statusColor = when (invoice.status) {
        InvoiceStatus.COMPLETED -> ProfitGreen
        InvoiceStatus.CREDIT -> StockLow
        InvoiceStatus.CANCELLED -> StockCritical
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Invoice icon with status color accent
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = statusColor.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Receipt,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = invoice.invoiceNumber,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!invoice.customerName.isNullOrBlank()) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = invoice.customerName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = dateFormatter.format(Date(invoice.createdAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = currencyFormatter.format(invoice.totalAmount),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = invoice.status.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

// ── Low Stock Item ──────────────────────────────────────────────────────────

@Composable
private fun LowStockItem(
    product: Product,
    onClick: () -> Unit
) {
    val stockRatio = if (product.minStockThreshold > 0) {
        product.currentStock / product.minStockThreshold
    } else 1.0

    val stockColor = when {
        product.currentStock <= 0 -> MaterialTheme.colorScheme.error
        stockRatio <= 0.5 -> StockCritical
        else -> StockLow
    }

    val stockLabel = when {
        product.currentStock <= 0 -> "OUT OF STOCK"
        stockRatio <= 0.5 -> "CRITICAL"
        else -> "LOW"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Warning icon
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = stockColor.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = stockColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Stock: ${product.currentStock.toInt()} / Min: ${product.minStockThreshold.toInt()} ${product.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = MaterialTheme.shapes.extraSmall,
                color = stockColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = stockLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = stockColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ── Empty State ─────────────────────────────────────────────────────────────

@Composable
private fun EmptyStateCard(
    message: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
