package com.khalid.cashlytics.presentation.reports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.khalid.cashlytics.presentation.navigation.Screen

private data class ReportCard(
    val type: ReportType,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    navController: NavController
) {
    val reportCards = remember {
        listOf(
            ReportCard(
                type = ReportType.SALES,
                title = "Sales",
                subtitle = "Revenue & transactions",
                icon = Icons.Filled.PointOfSale
            ),
            ReportCard(
                type = ReportType.PURCHASE,
                title = "Purchase",
                subtitle = "Purchase history",
                icon = Icons.Filled.ShoppingCart
            ),
            ReportCard(
                type = ReportType.PROFIT_LOSS,
                title = "Profit & Loss",
                subtitle = "Income vs expenses",
                icon = Icons.Filled.Assessment
            ),
            ReportCard(
                type = ReportType.INVENTORY_VALUATION,
                title = "Inventory Valuation",
                subtitle = "Stock value summary",
                icon = Icons.Filled.Inventory
            ),
            ReportCard(
                type = ReportType.CUSTOMER_LEDGER,
                title = "Customer Ledger",
                subtitle = "Outstanding balances",
                icon = Icons.Filled.People
            ),
            ReportCard(
                type = ReportType.VENDOR_PAYABLE,
                title = "Vendor Payable",
                subtitle = "Amounts owed to vendors",
                icon = Icons.Filled.AccountBalance
            ),
            ReportCard(
                type = ReportType.LOW_STOCK,
                title = "Low Stock",
                subtitle = "Items below threshold",
                icon = Icons.Filled.Warning
            ),
            ReportCard(
                type = ReportType.TOP_SELLING,
                title = "Top Selling",
                subtitle = "Best performing products",
                icon = Icons.Filled.Star
            ),
            ReportCard(
                type = ReportType.TAX,
                title = "Tax Report",
                subtitle = "Tax collected summary",
                icon = Icons.Filled.Receipt
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = reportCards,
                key = { it.type.name }
            ) { card ->
                ReportTypeCard(
                    card = card,
                    onClick = {
                        val route = when (card.type) {
                            ReportType.SALES -> Screen.SalesReport.route
                            ReportType.PURCHASE -> Screen.PurchaseReport.route
                            ReportType.PROFIT_LOSS -> Screen.ProfitLossReport.route
                            ReportType.INVENTORY_VALUATION -> Screen.InventoryValuationReport.route
                            ReportType.CUSTOMER_LEDGER -> Screen.CustomerLedgerReport.route
                            ReportType.VENDOR_PAYABLE -> Screen.VendorPayableReport.route
                            ReportType.LOW_STOCK -> Screen.LowStockReport.route
                            ReportType.TOP_SELLING -> Screen.TopSellingReport.route
                            ReportType.TAX -> Screen.TaxReport.route
                        }
                        navController.navigate(route)
                    }
                )
            }
        }
    }
}

@Composable
private fun ReportTypeCard(
    card: ReportCard,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = card.icon,
                contentDescription = card.title,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = card.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = card.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
