package com.khalid.vyntra.presentation.billing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.khalid.vyntra.presentation.components.LoadingIndicator
import com.khalid.vyntra.presentation.navigation.Screen
import com.khalid.vyntra.presentation.theme.ChipShape
import com.khalid.vyntra.presentation.theme.ProfitGreen
import com.khalid.vyntra.presentation.theme.SearchBarShape
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
fun InvoiceHistoryScreen(
    navController: NavController,
    viewModel: InvoiceHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is InvoiceHistoryEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is InvoiceHistoryEvent.InvoiceCancelled -> {
                    snackbarHostState.showSnackbar("Invoice ${event.invoiceNumber} cancelled")
                }
            }
        }
    }

    if (showDatePicker) {
        VyntraDateRangePicker(
            onDismiss = { showDatePicker = false },
            onConfirm = { start, end ->
                viewModel.onDateRangeChanged(start, end)
                showDatePicker = false
            },
            initialStartMillis = uiState.startDate,
            initialEndMillis = uiState.endDate
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search by invoice number
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search by invoice # or customer...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = uiState.searchQuery.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = SearchBarShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Date filter chips
            DateFilterChips(
                startDate = uiState.startDate,
                endDate = uiState.endDate,
                onDateRangeChanged = viewModel::onDateRangeChanged,
                onShowCustomPicker = { showDatePicker = true }
            )

            // Status filter chips
            StatusFilterChips(
                selectedStatus = uiState.statusFilter,
                onStatusSelected = viewModel::onStatusFilterChanged
            )

            // Invoice list
            when {
                uiState.isLoading -> {
                    LoadingIndicator()
                }

                uiState.invoices.isEmpty() -> {
                    EmptyStateView(
                        icon = Icons.Default.ReceiptLong,
                        title = "No invoices found",
                        message = if (uiState.searchQuery.isNotEmpty() ||
                            uiState.statusFilter != null ||
                            uiState.startDate != null
                        ) {
                            "Try adjusting your filters"
                        } else {
                            "Create your first sale to see invoices here"
                        },
                        actionLabel = if (uiState.searchQuery.isNotEmpty() ||
                            uiState.statusFilter != null ||
                            uiState.startDate != null
                        ) {
                            "Clear Filters"
                        } else null,
                        onAction = { viewModel.clearFilters() }
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.invoices,
                            key = { it.id }
                        ) { invoice ->
                            InvoiceCard(
                                invoice = invoice,
                                onClick = {
                                    navController.navigate(
                                        Screen.InvoiceDetail.createRoute(invoice.id)
                                    )
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateFilterChips(
    startDate: Long?,
    endDate: Long?,
    onDateRangeChanged: (Long?, Long?) -> Unit,
    onShowCustomPicker: () -> Unit
) {
    val now = System.currentTimeMillis()
    val today = LocalDate.now()
    val todayStart = startOfDay(now)
    val todayEnd = endOfDay(now)

    val weekStart = startOfDay(
        today.minusDays(today.dayOfWeek.value.toLong() - 1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    val monthStart = startOfDay(
        today.withDayOfMonth(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    data class DateChip(val label: String, val start: Long?, val end: Long?)
    val chips = listOf(
        DateChip("All", null, null),
        DateChip("Today", todayStart, todayEnd),
        DateChip("This Week", weekStart, todayEnd),
        DateChip("This Month", monthStart, todayEnd),
        DateChip("Custom", -1L, -1L)
    )

    val selectedLabel = when {
        startDate == null && endDate == null -> "All"
        startDate == todayStart && endDate == todayEnd -> "Today"
        startDate == weekStart && endDate == todayEnd -> "This Week"
        startDate == monthStart && endDate == todayEnd -> "This Month"
        startDate != null && endDate != null -> "Custom"
        else -> "All"
    }

    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chips.forEach { chip ->
            FilterChip(
                selected = chip.label == selectedLabel,
                onClick = {
                    if (chip.label == "Custom") {
                        onShowCustomPicker()
                    } else {
                        onDateRangeChanged(chip.start, chip.end)
                    }
                },
                label = {
                    Text(
                        text = chip.label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                shape = ChipShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
private fun StatusFilterChips(
    selectedStatus: InvoiceStatus?,
    onStatusSelected: (InvoiceStatus?) -> Unit
) {
    data class StatusChip(val label: String, val status: InvoiceStatus?)
    val chips = listOf(
        StatusChip("All", null),
        StatusChip("Completed", InvoiceStatus.COMPLETED),
        StatusChip("Credit", InvoiceStatus.CREDIT),
        StatusChip("Cancelled", InvoiceStatus.CANCELLED)
    )

    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chips.forEach { chip ->
            FilterChip(
                selected = chip.status == selectedStatus,
                onClick = { onStatusSelected(chip.status) },
                label = {
                    Text(
                        text = chip.label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                shape = ChipShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    }
}

@Composable
private fun InvoiceCard(
    invoice: Invoice,
    onClick: () -> Unit
) {
    val statusColor = when (invoice.status) {
        InvoiceStatus.COMPLETED -> ProfitGreen
        InvoiceStatus.CREDIT -> StockLow
        InvoiceStatus.CANCELLED -> StockCritical
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
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
            // Invoice icon
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = statusColor.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Invoice info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = invoice.invoiceNumber,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!invoice.customerName.isNullOrBlank()) {
                        Icon(
                            imageVector = Icons.Default.Person,
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
                        text = invoice.createdAt.toFormattedDateTime(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Total and status badge
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = invoice.totalAmount.toCurrency(),
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
