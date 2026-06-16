package com.khalid.vyntra.presentation.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.khalid.vyntra.domain.model.Invoice
import com.khalid.vyntra.domain.model.InvoiceStatus
import com.khalid.vyntra.presentation.components.VyntraDateRangePicker
import com.khalid.vyntra.presentation.components.EmptyStateView
import com.khalid.vyntra.presentation.components.FilterChipItem
import com.khalid.vyntra.presentation.components.FilterChipsRow
import com.khalid.vyntra.presentation.components.LoadingIndicator
import com.khalid.vyntra.presentation.components.StatCard
import com.khalid.vyntra.presentation.navigation.Screen
import com.khalid.vyntra.presentation.theme.ProfitGreen
import com.khalid.vyntra.presentation.theme.StockCritical
import com.khalid.vyntra.presentation.theme.StockLow
import com.khalid.vyntra.util.endOfDay
import com.khalid.vyntra.util.startOfDay
import com.khalid.vyntra.util.toCurrency
import com.khalid.vyntra.util.toFormattedDate
import com.khalid.vyntra.util.toFormattedDateTime
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesReportScreen(
    navController: NavController,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val filterChips = remember {
        listOf(
            FilterChipItem("Today", "today"),
            FilterChipItem("This Week", "week"),
            FilterChipItem("This Month", "month"),
            FilterChipItem("Custom", "custom")
        )
    }

    var selectedFilter by remember { mutableStateOf("month") }
    var showDatePicker by remember { mutableStateOf(false) }

    // Apply date range when filter changes
    LaunchedEffect(selectedFilter) {
        val now = LocalDate.now()
        val zone = ZoneId.systemDefault()

        when (selectedFilter) {
            "today" -> {
                val todayStart = startOfDay(System.currentTimeMillis())
                val todayEnd = endOfDay(System.currentTimeMillis())
                viewModel.setDateRange(todayStart, todayEnd)
            }
            "week" -> {
                val startOfWeek = now.minusDays(now.dayOfWeek.value.toLong() - 1)
                val start = startOfWeek.atStartOfDay(zone).toInstant().toEpochMilli()
                val end = endOfDay(System.currentTimeMillis())
                viewModel.setDateRange(start, end)
            }
            "month" -> {
                val startOfMonth = now.withDayOfMonth(1)
                val start = startOfMonth.atStartOfDay(zone).toInstant().toEpochMilli()
                val end = endOfDay(System.currentTimeMillis())
                viewModel.setDateRange(start, end)
            }
            "custom" -> {
                showDatePicker = true
            }
        }
        viewModel.loadReport(ReportType.SALES)
    }

    if (showDatePicker) {
        VyntraDateRangePicker(
            onDismiss = {
                showDatePicker = false
                if (selectedFilter == "custom" && uiState.salesData == null) {
                    selectedFilter = "month"
                }
            },
            onConfirm = { start, end ->
                viewModel.setDateRange(start, endOfDay(end))
                viewModel.loadReport(ReportType.SALES)
                showDatePicker = false
            },
            initialStartMillis = uiState.dateRange.startDate,
            initialEndMillis = uiState.dateRange.endDate
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales Report") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Export PDF */ }) {
                        Icon(
                            imageVector = Icons.Filled.PictureAsPdf,
                            contentDescription = "Export PDF"
                        )
                    }
                    IconButton(onClick = { /* Export CSV */ }) {
                        Icon(
                            imageVector = Icons.Filled.TableChart,
                            contentDescription = "Export CSV"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            LoadingIndicator(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Date filter chips
                item {
                    FilterChipsRow(
                        chips = filterChips,
                        selectedKey = selectedFilter,
                        onSelected = { selectedFilter = it }
                    )
                }

                // Date range label
                item {
                    Text(
                        text = "${uiState.dateRange.startDate.toFormattedDate()} - ${uiState.dateRange.endDate.toFormattedDate()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                // Summary cards
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    val salesData = uiState.salesData

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            icon = Icons.Filled.AttachMoney,
                            value = (salesData?.totalSales ?: 0.0).toCurrency(),
                            label = "Total Sales",
                            modifier = Modifier.weight(1f),
                            iconTint = ProfitGreen
                        )
                        StatCard(
                            icon = Icons.Filled.Numbers,
                            value = (salesData?.totalTransactions ?: 0).toString(),
                            label = "Transactions",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            icon = Icons.Filled.PointOfSale,
                            value = (uiState.salesData?.averageSale ?: 0.0).toCurrency(),
                            label = "Average Sale",
                            modifier = Modifier.weight(1f)
                        )
                        // Placeholder for symmetry
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                // Daily sales trend chart — grouped by formatted date so days
                // with no sales drop out naturally (sparser X-axis).
                val invoices = uiState.salesData?.invoices ?: emptyList()
                if (invoices.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        val byDay = invoices
                            .groupBy { it.createdAt.toFormattedDate() }
                            .toSortedMap()
                        com.khalid.vyntra.presentation.components.TrendLineChart(
                            title = "Daily Sales",
                            values = byDay.values.map { day -> day.sumOf { it.totalAmount } },
                            xLabels = byDay.keys.toList()
                        )
                    }
                }

                // Sales list header
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                if (invoices.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Filled.Receipt,
                            title = "No sales found",
                            message = "No transactions in the selected date range."
                        )
                    }
                } else {
                    // Group by date
                    val grouped = invoices.groupBy { it.createdAt.toFormattedDate() }

                    grouped.forEach { (dateLabel, dayInvoices) ->
                        item {
                            Text(
                                text = dateLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                )
                            )
                        }

                        items(
                            items = dayInvoices,
                            key = { it.id }
                        ) { invoice ->
                            SalesInvoiceItem(
                                invoice = invoice,
                                onClick = {
                                    navController.navigate(
                                        Screen.InvoiceDetail.createRoute(invoice.id)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SalesInvoiceItem(
    invoice: Invoice,
    onClick: () -> Unit
) {
    val statusColor = when (invoice.status) {
        InvoiceStatus.COMPLETED -> ProfitGreen
        InvoiceStatus.CREDIT -> StockLow
        InvoiceStatus.CANCELLED -> StockCritical
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = invoice.invoiceNumber,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (!invoice.customerName.isNullOrBlank()) {
                    Text(
                        text = invoice.customerName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = invoice.createdAt.toFormattedDateTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = invoice.totalAmount.toCurrency(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
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
