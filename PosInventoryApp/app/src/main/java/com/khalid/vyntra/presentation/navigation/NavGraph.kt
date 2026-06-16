package com.khalid.vyntra.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.khalid.vyntra.presentation.billing.InvoiceDetailScreen
import com.khalid.vyntra.presentation.billing.InvoiceHistoryScreen
import com.khalid.vyntra.presentation.billing.NewSaleScreen
import com.khalid.vyntra.presentation.category.CategoryScreen
import com.khalid.vyntra.presentation.customer.AddEditCustomerScreen
import com.khalid.vyntra.presentation.customer.CustomerDetailScreen
import com.khalid.vyntra.presentation.customer.CustomerListScreen
import com.khalid.vyntra.presentation.dashboard.DashboardScreen
import com.khalid.vyntra.presentation.inventory.AddEditProductScreen
import com.khalid.vyntra.presentation.inventory.InventoryScreen
import com.khalid.vyntra.presentation.purchase.NewPurchaseScreen
import com.khalid.vyntra.presentation.purchase.PurchaseDetailScreen
import com.khalid.vyntra.presentation.purchase.PurchaseHistoryScreen
import com.khalid.vyntra.presentation.reports.GeneralReportScreen
import com.khalid.vyntra.presentation.reports.ReportType
import com.khalid.vyntra.presentation.reports.ReportsScreen
import com.khalid.vyntra.presentation.reports.SalesReportScreen
import com.khalid.vyntra.presentation.csv.CsvExportScreen
import com.khalid.vyntra.presentation.csv.CsvImportScreen
import com.khalid.vyntra.presentation.premium.PremiumScreen
import com.khalid.vyntra.presentation.scanner.BarcodeScannerScreen
import com.khalid.vyntra.presentation.settings.AboutScreen
import com.khalid.vyntra.presentation.settings.BusinessProfileScreen
import com.khalid.vyntra.presentation.settings.DataScreen
import com.khalid.vyntra.presentation.settings.DisplaySettingsScreen
import com.khalid.vyntra.presentation.settings.InvoiceSettingsScreen
import com.khalid.vyntra.presentation.settings.SettingsScreen
import com.khalid.vyntra.presentation.stock.StockAdjustmentScreen
import com.khalid.vyntra.presentation.vendor.AddEditVendorScreen
import com.khalid.vyntra.presentation.vendor.VendorDetailScreen
import com.khalid.vyntra.presentation.vendor.VendorListScreen

@Composable
fun VyntraNavGraph(
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
                navDeepLink { uriPattern = "vyntra://product/edit/{productId}" }
            )
        ) {
            AddEditProductScreen(navController = navController)
        }

        // Categories
        composable(route = Screen.Categories.route) {
            CategoryScreen(navController = navController)
        }

        // Customers — these screens use callback-based navigation, not navController.
        composable(route = Screen.Customers.route) {
            CustomerListScreen(
                viewModel = androidx.hilt.navigation.compose.hiltViewModel(),
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddCustomer = {
                    navController.navigate(Screen.AddEditCustomer.createRoute())
                },
                onNavigateToCustomerDetail = { customerId ->
                    navController.navigate(Screen.CustomerDetail.createRoute(customerId))
                }
            )
        }

        composable(
            route = Screen.CustomerDetail.route,
            arguments = listOf(
                navArgument("customerId") { type = NavType.LongType }
            )
        ) {
            CustomerDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditCustomer = { customerId ->
                    navController.navigate(Screen.AddEditCustomer.createRoute(customerId))
                }
            )
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
            AddEditCustomerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Vendors — same callback pattern as Customers.
        composable(route = Screen.Vendors.route) {
            VendorListScreen(
                viewModel = androidx.hilt.navigation.compose.hiltViewModel(),
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddVendor = {
                    navController.navigate(Screen.AddEditVendor.createRoute())
                },
                onNavigateToVendorDetail = { vendorId ->
                    navController.navigate(Screen.VendorDetail.createRoute(vendorId))
                }
            )
        }

        composable(
            route = Screen.VendorDetail.route,
            arguments = listOf(
                navArgument("vendorId") { type = NavType.LongType }
            )
        ) {
            VendorDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditVendor = { vendorId ->
                    navController.navigate(Screen.AddEditVendor.createRoute(vendorId))
                }
            )
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
            AddEditVendorScreen(
                onNavigateBack = { navController.popBackStack() }
            )
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

        // Settings hub + sub-screens.
        composable(route = Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(route = Screen.SettingsBusinessProfile.route) {
            BusinessProfileScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(route = Screen.SettingsInvoice.route) {
            InvoiceSettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(route = Screen.SettingsDisplay.route) {
            DisplaySettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(route = Screen.SettingsData.route) {
            DataScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToExport = { navController.navigate(Screen.CsvExport.route) },
                onNavigateToImport = { navController.navigate(Screen.CsvImport.route) }
            )
        }
        composable(route = Screen.SettingsAbout.route) {
            AboutScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Premium upgrade
        composable(route = Screen.Premium.route) {
            PremiumScreen(onNavigateBack = { navController.popBackStack() })
        }

        // CSV import/export
        composable(route = Screen.CsvExport.route) {
            CsvExportScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(route = Screen.CsvImport.route) {
            CsvImportScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Barcode Scanner
        composable(route = Screen.BarcodeScanner.route) {
            BarcodeScannerScreen(navController = navController)
        }
    }
}
