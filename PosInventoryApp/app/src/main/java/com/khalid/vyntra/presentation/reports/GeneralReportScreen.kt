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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.khalid.vyntra.domain.model.Customer
import com.khalid.vyntra.domain.model.Product
import com.khalid.vyntra.domain.model.Purchase
import com.khalid.vyntra.domain.model.Vendor
import com.khalid.vyntra.presentation.components.EmptyStateView
import com.khalid.vyntra.presentation.components.LoadingIndicator
import com.khalid.vyntra.presentation.components.StatCard
import com.khalid.vyntra.presentation.components.StockBadge
import com.khalid.vyntra.presentation.components.resolveStockStatus
import com.khalid.vyntra.presentation.theme.LossRed
import com.khalid.vyntra.presentation.theme.ProfitGreen
import com.khalid.vyntra.presentation.theme.StockLow
import com.khalid.vyntra.util.toCurrency
import com.khalid.vyntra.util.toFormattedDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralReportScreen(
    navController: NavController,
    reportType: ReportType,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(reportType) {
        viewModel.loadReport(reportType)
    }

    val title = when (reportType) {
        ReportType.PURCHASE -> "Purchase Report"
        ReportType.PROFIT_LOSS -> "Profit & Loss"
        ReportType.INVENTORY_VALUATION -> "Inventory Valuation"
        ReportType.CUSTOMER_LEDGER -> "Customer Ledger"
        ReportType.VENDOR_PAYABLE -> "Vendor Payable"
        ReportType.LOW_STOCK -> "Low Stock Report"
        ReportType.TOP_SELLING -> "Top Selling Products"
        ReportType.TAX -> "Tax Report"
        else -> "Report"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            LoadingIndicator(modifier = Modifier.padding(innerPadding))
        } else {
            when (reportType) {
                ReportType.PURCHASE -> PurchaseReportContent(
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding)
                )
                ReportType.PROFIT_LOSS -> ProfitLossContent(
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding)
                )
                ReportType.INVENTORY_VALUATION -> InventoryValuationContent(
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding)
                )
                ReportType.CUSTOMER_LEDGER -> CustomerLedgerContent(
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding)
                )
                ReportType.VENDOR_PAYABLE -> VendorPayableContent(
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding)
                )
                ReportType.LOW_STOCK -> LowStockContent(
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding)
                )
                ReportType.TOP_SELLING -> TopSellingContent(
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding)
                )
                ReportType.TAX -> TaxReportContent(
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding)
                )
                else -> { /* Sales handled by SalesReportScreen */ }
            }
        }
    }
}

// ── Purchase Report ────────────────────────────────────────────────────────────

@Composable
private fun PurchaseReportContent(
    uiState: ReportsUiState,
    modifier: Modifier = Modifier
) {
    val data = uiState.purchaseData

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    icon = Icons.Filled.ShoppingCart,
                    value = (data?.totalPurchases ?: 0.0).toCurrency(),
                    label = "Total Purchases",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Filled.Numbers,
                    value = (data?.totalTransactions ?: 0).toString(),
                    label = "Transactions",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Purchase History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        val purchases = data?.purchases ?: emptyList()

        if (purchases.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Filled.ShoppingCart,
                    title = "No purchases found",
                    message = "No purchase records in the selected period."
                )
            }
        } else {
            items(
                items = purchases,
                key = { it.id }
            ) { purchase ->
                PurchaseListItem(purchase = purchase)
            }
        }
    }
}

@Composable
private fun PurchaseListItem(purchase: Purchase) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
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
                    text = purchase.vendorName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = purchase.createdAt.toFormattedDate(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${purchase.items.size} item(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = purchase.totalAmount.toCurrency(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ── Profit & Loss ──────────────────────────────────────────────────────────────

@Composable
private fun ProfitLossContent(
    uiState: ReportsUiState,
    modifier: Modifier = Modifier
) {
    val data = uiState.profitLossData

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "${uiState.dateRange.startDate.toFormattedDate()} - ${uiState.dateRange.endDate.toFormattedDate()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    icon = Icons.Filled.TrendingUp,
                    value = (data?.totalSales ?: 0.0).toCurrency(),
                    label = "Total Income",
                    modifier = Modifier.weight(1f),
                    iconTint = ProfitGreen
                )
                StatCard(
                    icon = Icons.Filled.TrendingDown,
                    value = (data?.totalPurchases ?: 0.0).toCurrency(),
                    label = "Total Expenses",
                    modifier = Modifier.weight(1f),
                    iconTint = LossRed
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    icon = Icons.Filled.AttachMoney,
                    value = (data?.grossProfit ?: 0.0).toCurrency(),
                    label = "Gross Profit",
                    modifier = Modifier.weight(1f),
                    iconTint = if ((data?.grossProfit ?: 0.0) >= 0) ProfitGreen else LossRed
                )
                StatCard(
                    icon = Icons.Filled.Receipt,
                    value = (data?.totalTax ?: 0.0).toCurrency(),
                    label = "Tax Collected",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            val netProfit = data?.netProfit ?: 0.0
            StatCard(
                icon = Icons.Filled.AccountBalance,
                value = netProfit.toCurrency(),
                label = "Net Profit",
                modifier = Modifier.fillMaxWidth(),
                iconTint = if (netProfit >= 0) ProfitGreen else LossRed,
                subtitle = if (netProfit >= 0) "Your business is profitable" else "Expenses exceed income"
            )
        }
    }
}

// ── Inventory Valuation ────────────────────────────────────────────────────────

@Composable
private fun InventoryValuationContent(
    uiState: ReportsUiState,
    modifier: Modifier = Modifier
) {
    val data = uiState.inventoryValuationData

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    icon = Icons.Filled.AttachMoney,
                    value = (data?.totalValue ?: 0.0).toCurrency(),
                    label = "Total Inventory Value",
                    modifier = Modifier.weight(1f),
                    iconTint = ProfitGreen
                )
                StatCard(
                    icon = Icons.Filled.Inventory,
                    value = (data?.totalItems ?: 0).toString(),
                    label = "Total Items",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Products",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        val products = data?.products ?: emptyList()

        if (products.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Filled.Inventory,
                    title = "No products",
                    message = "Add products to see inventory valuation."
                )
            }
        } else {
            items(
                items = products,
                key = { it.id }
            ) { product ->
                InventoryProductItem(product = product)
            }
        }
    }
}

@Composable
private fun InventoryProductItem(product: Product) {
    val stockValue = product.purchasePrice * product.currentStock

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
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
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Stock: ${product.currentStock.toInt()} x ${product.purchasePrice.toCurrency()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stockValue.toCurrency(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ── Customer Ledger ────────────────────────────────────────────────────────────

@Composable
private fun CustomerLedgerContent(
    uiState: ReportsUiState,
    modifier: Modifier = Modifier
) {
    val customers = uiState.customerLedger

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        if (customers.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                val totalOutstanding = customers.sumOf { it.outstandingBalance }
                StatCard(
                    icon = Icons.Filled.People,
                    value = totalOutstanding.toCurrency(),
                    label = "Total Outstanding",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    iconTint = LossRed,
                    subtitle = "${customers.size} customer(s) with outstanding balance"
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Customers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (customers.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Filled.People,
                    title = "No outstanding balances",
                    message = "All customers have cleared their dues."
                )
            }
        } else {
            items(
                items = customers,
                key = { it.id }
            ) { customer ->
                CustomerLedgerItem(customer = customer)
            }
        }
    }
}

@Composable
private fun CustomerLedgerItem(customer: Customer) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
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
                    text = customer.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!customer.phone.isNullOrBlank()) {
                    Text(
                        text = customer.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = customer.outstandingBalance.toCurrency(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = LossRed
                )
                Text(
                    text = "Outstanding",
                    style = MaterialTheme.typography.labelSmall,
                    color = LossRed
                )
            }
        }
    }
}

// ── Vendor Payable ─────────────────────────────────────────────────────────────

@Composable
private fun VendorPayableContent(
    uiState: ReportsUiState,
    modifier: Modifier = Modifier
) {
    val vendors = uiState.vendorPayables

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        if (vendors.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                val totalPayable = vendors.sumOf { it.currentPayableBalance }
                StatCard(
                    icon = Icons.Filled.AccountBalance,
                    value = totalPayable.toCurrency(),
                    label = "Total Payable",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    iconTint = LossRed,
                    subtitle = "${vendors.size} vendor(s) with outstanding payable"
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Vendors",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (vendors.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Filled.AccountBalance,
                    title = "No vendor payables",
                    message = "All vendors have been paid."
                )
            }
        } else {
            items(
                items = vendors,
                key = { it.id }
            ) { vendor ->
                VendorPayableItem(vendor = vendor)
            }
        }
    }
}

@Composable
private fun VendorPayableItem(vendor: Vendor) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
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
                    text = vendor.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!vendor.company.isNullOrBlank()) {
                    Text(
                        text = vendor.company,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = vendor.currentPayableBalance.toCurrency(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = LossRed
                )
                Text(
                    text = "Payable",
                    style = MaterialTheme.typography.labelSmall,
                    color = LossRed
                )
            }
        }
    }
}

// ── Low Stock ──────────────────────────────────────────────────────────────────

@Composable
private fun LowStockContent(
    uiState: ReportsUiState,
    modifier: Modifier = Modifier
) {
    val products = uiState.lowStockProducts

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        if (products.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                StatCard(
                    icon = Icons.Filled.Warning,
                    value = products.size.toString(),
                    label = "Products Below Threshold",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    iconTint = StockLow
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Low Stock Items",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (products.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Filled.Inventory,
                    title = "All stocked up",
                    message = "No products are below their minimum stock threshold."
                )
            }
        } else {
            items(
                items = products,
                key = { it.id }
            ) { product ->
                LowStockProductItem(product = product)
            }
        }
    }
}

@Composable
private fun LowStockProductItem(product: Product) {
    val status = resolveStockStatus(product.currentStock, product.minStockThreshold)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
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
            StockBadge(status = status)
        }
    }
}

// ── Top Selling ────────────────────────────────────────────────────────────────

@Composable
private fun TopSellingContent(
    uiState: ReportsUiState,
    modifier: Modifier = Modifier
) {
    val products = uiState.topSellingProducts

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${uiState.dateRange.startDate.toFormattedDate()} - ${uiState.dateRange.endDate.toFormattedDate()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Top Selling Products",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (products.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Filled.Star,
                    title = "No data available",
                    message = "Complete some sales to see top selling products."
                )
            }
        } else {
            itemsIndexed(
                items = products,
                key = { index, _ -> index }
            ) { index, (productName, quantity) ->
                TopSellingItem(
                    rank = index + 1,
                    productName = productName,
                    quantity = quantity
                )
            }
        }
    }
}

@Composable
private fun TopSellingItem(
    rank: Int,
    productName: String,
    quantity: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge
            Surface(
                shape = MaterialTheme.shapes.small,
                color = when (rank) {
                    1 -> MaterialTheme.colorScheme.primaryContainer
                    2 -> MaterialTheme.colorScheme.secondaryContainer
                    3 -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                }
            ) {
                Text(
                    text = "#$rank",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    color = when (rank) {
                        1 -> MaterialTheme.colorScheme.onPrimaryContainer
                        2 -> MaterialTheme.colorScheme.onSecondaryContainer
                        3 -> MaterialTheme.colorScheme.onTertiaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = productName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = quantity.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "units sold",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Tax Report ─────────────────────────────────────────────────────────────────

@Composable
private fun TaxReportContent(
    uiState: ReportsUiState,
    modifier: Modifier = Modifier
) {
    val salesData = uiState.salesData
    val taxCollected = uiState.taxCollected

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "${uiState.dateRange.startDate.toFormattedDate()} - ${uiState.dateRange.endDate.toFormattedDate()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            StatCard(
                icon = Icons.Filled.Receipt,
                value = taxCollected.toCurrency(),
                label = "Total Tax Collected",
                modifier = Modifier.fillMaxWidth(),
                iconTint = MaterialTheme.colorScheme.primary,
                subtitle = "From all completed transactions"
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tax Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Reload sales to get per-invoice tax if needed
        val invoices = uiState.salesData?.invoices ?: emptyList()
        val taxInvoices = invoices.filter { it.taxAmount > 0 }

        if (taxInvoices.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Filled.Receipt,
                    title = "No tax records",
                    message = "No tax was collected in the selected period."
                )
            }
        } else {
            items(
                items = taxInvoices,
                key = { it.id }
            ) { invoice ->
                TaxInvoiceItem(invoice = invoice)
            }
        }
    }
}

@Composable
private fun TaxInvoiceItem(invoice: com.khalid.vyntra.domain.model.Invoice) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
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
                Text(
                    text = invoice.createdAt.toFormattedDate(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Subtotal: ${invoice.subtotal.toCurrency()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = invoice.taxAmount.toCurrency(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${invoice.taxPercent}% tax",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
