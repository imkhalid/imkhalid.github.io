package com.khalid.cashlytics.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.khalid.cashlytics.presentation.billing.InvoiceDetailScreen
import com.khalid.cashlytics.presentation.billing.InvoiceHistoryScreen
import com.khalid.cashlytics.presentation.billing.NewSaleScreen
import com.khalid.cashlytics.presentation.category.CategoryScreen
import com.khalid.cashlytics.presentation.customer.AddEditCustomerScreen
import com.khalid.cashlytics.presentation.customer.CustomerDetailScreen
import com.khalid.cashlytics.presentation.customer.CustomerListScreen
import com.khalid.cashlytics.presentation.dashboard.DashboardScreen
import com.khalid.cashlytics.presentation.inventory.AddEditProductScreen
import com.khalid.cashlytics.presentation.inventory.InventoryScreen
import com.khalid.cashlytics.presentation.purchase.NewPurchaseScreen
import com.khalid.cashlytics.presentation.purchase.PurchaseDetailScreen
import com.khalid.cashlytics.presentation.purchase.PurchaseHistoryScreen
import com.khalid.cashlytics.presentation.reports.GeneralReportScreen
import com.khalid.cashlytics.presentation.reports.ReportType
import com.khalid.cashlytics.presentation.reports.ReportsScreen
import com.khalid.cashlytics.presentation.reports.SalesReportScreen
import com.khalid.cashlytics.presentation.scanner.BarcodeScannerScreen
import com.khalid.cashlytics.presentation.settings.SettingsScreen
import com.khalid.cashlytics.presentation.stock.StockAdjustmentScreen
import com.khalid.cashlytics.presentation.vendor.AddEditVendorScreen
import com.khalid.cashlytics.presentation.vendor.VendorDetailScreen
import com.khalid.cashlytics.presentation.vendor.VendorListScreen

@Composable
fun CashlyticsNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {

        // Dashboard
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }

        // Inventory
        composable(route = Screen.Inventory.route) {
            InventoryScreen(navController = navController)
        }

        composable(
            route = Screen.AddEditProduct.route,
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "cashlytics://product/edit/{productId}" }
            )
        ) {
            AddEditProductScreen(navController = navController)
        }

        // Categories
        composable(route = Screen.Categories.route) {
            CategoryScreen(navController = navController)
        }

        // Customers
        composable(route = Screen.Customers.route) {
            CustomerListScreen(navController = navController)
        }

        composable(
            route = Screen.CustomerDetail.route,
            arguments = listOf(
                navArgument("customerId") { type = NavType.LongType }
            )
        ) {
            CustomerDetailScreen(navController = navController)
        }

        composable(
            route = Screen.AddEditCustomer.route,
            arguments = listOf(
                navArgument("customerId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            AddEditCustomerScreen(navController = navController)
        }

        // Vendors
        composable(route = Screen.Vendors.route) {
            VendorListScreen(navController = navController)
        }

        composable(
            route = Screen.VendorDetail.route,
            arguments = listOf(
                navArgument("vendorId") { type = NavType.LongType }
            )
        ) {
            VendorDetailScreen(navController = navController)
        }

        composable(
            route = Screen.AddEditVendor.route,
            arguments = listOf(
                navArgument("vendorId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            AddEditVendorScreen(navController = navController)
        }

        // Sales / Invoices
        composable(route = Screen.NewSale.route) {
            NewSaleScreen(navController = navController)
        }

        composable(
            route = Screen.InvoiceDetail.route,
            arguments = listOf(
                navArgument("invoiceId") { type = NavType.LongType }
            )
        ) {
            InvoiceDetailScreen(navController = navController)
        }

        composable(route = Screen.InvoiceHistory.route) {
            InvoiceHistoryScreen(navController = navController)
        }

        // Purchases
        composable(route = Screen.NewPurchase.route) {
            NewPurchaseScreen(navController = navController)
        }

        composable(
            route = Screen.PurchaseDetail.route,
            arguments = listOf(
                navArgument("purchaseId") { type = NavType.LongType }
            )
        ) {
            PurchaseDetailScreen(navController = navController)
        }

        composable(route = Screen.PurchaseHistory.route) {
            PurchaseHistoryScreen(navController = navController)
        }

        // Stock Adjustment
        composable(
            route = Screen.StockAdjustment.route,
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            StockAdjustmentScreen(navController = navController)
        }

        // Reports
        composable(route = Screen.Reports.route) {
            ReportsScreen(navController = navController)
        }

        composable(route = Screen.SalesReport.route) {
            SalesReportScreen(navController = navController)
        }

        composable(route = Screen.PurchaseReport.route) {
            GeneralReportScreen(
                navController = navController,
                reportType = ReportType.PURCHASE
            )
        }

        composable(route = Screen.ProfitLossReport.route) {
            GeneralReportScreen(
                navController = navController,
                reportType = ReportType.PROFIT_LOSS
            )
        }

        composable(route = Screen.InventoryValuationReport.route) {
            GeneralReportScreen(
                navController = navController,
                reportType = ReportType.INVENTORY_VALUATION
            )
        }

        composable(route = Screen.CustomerLedgerReport.route) {
            GeneralReportScreen(
                navController = navController,
                reportType = ReportType.CUSTOMER_LEDGER
            )
        }

        composable(route = Screen.VendorPayableReport.route) {
            GeneralReportScreen(
                navController = navController,
                reportType = ReportType.VENDOR_PAYABLE
            )
        }

        composable(route = Screen.LowStockReport.route) {
            GeneralReportScreen(
                navController = navController,
                reportType = ReportType.LOW_STOCK
            )
        }

        composable(route = Screen.TopSellingReport.route) {
            GeneralReportScreen(
                navController = navController,
                reportType = ReportType.TOP_SELLING
            )
        }

        composable(route = Screen.TaxReport.route) {
            GeneralReportScreen(
                navController = navController,
                reportType = ReportType.TAX
            )
        }

        // Settings
        composable(route = Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }

        // Barcode Scanner
        composable(route = Screen.BarcodeScanner.route) {
            BarcodeScannerScreen(navController = navController)
        }
    }
}
