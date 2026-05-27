package com.khalid.cashlytics.presentation.navigation

sealed class Screen(val route: String) {

    // ── Dashboard ───────────────────────────────────────────────────────
    data object Dashboard : Screen("dashboard")

    // ── Inventory / Products ────────────────────────────────────────────
    data object Inventory : Screen("inventory")
    data object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: Long) = "product_detail/$productId"
    }
    data object AddEditProduct : Screen("add_edit_product?productId={productId}") {
        fun createRoute(productId: Long? = null): String {
            return if (productId != null) "add_edit_product?productId=$productId"
            else "add_edit_product"
        }
    }

    // ── Categories ──────────────────────────────────────────────────────
    data object Categories : Screen("categories")
    data object AddEditCategory : Screen("add_edit_category?categoryId={categoryId}") {
        fun createRoute(categoryId: Long? = null): String {
            return if (categoryId != null) "add_edit_category?categoryId=$categoryId"
            else "add_edit_category"
        }
    }

    // ── Customers ───────────────────────────────────────────────────────
    data object Customers : Screen("customers")
    data object CustomerDetail : Screen("customer_detail/{customerId}") {
        fun createRoute(customerId: Long) = "customer_detail/$customerId"
    }
    data object AddEditCustomer : Screen("add_edit_customer?customerId={customerId}") {
        fun createRoute(customerId: Long? = null): String {
            return if (customerId != null) "add_edit_customer?customerId=$customerId"
            else "add_edit_customer"
        }
    }

    // ── Vendors ─────────────────────────────────────────────────────────
    data object Vendors : Screen("vendors")
    data object VendorDetail : Screen("vendor_detail/{vendorId}") {
        fun createRoute(vendorId: Long) = "vendor_detail/$vendorId"
    }
    data object AddEditVendor : Screen("add_edit_vendor?vendorId={vendorId}") {
        fun createRoute(vendorId: Long? = null): String {
            return if (vendorId != null) "add_edit_vendor?vendorId=$vendorId"
            else "add_edit_vendor"
        }
    }

    // ── Sales / Invoices ────────────────────────────────────────────────
    data object NewSale : Screen("new_sale")
    data object InvoiceDetail : Screen("invoice_detail/{invoiceId}") {
        fun createRoute(invoiceId: Long) = "invoice_detail/$invoiceId"
    }
    data object InvoiceHistory : Screen("invoice_history")

    // ── Purchases ───────────────────────────────────────────────────────
    data object NewPurchase : Screen("new_purchase")
    data object PurchaseDetail : Screen("purchase_detail/{purchaseId}") {
        fun createRoute(purchaseId: Long) = "purchase_detail/$purchaseId"
    }
    data object PurchaseHistory : Screen("purchase_history")

    // ── Stock ───────────────────────────────────────────────────────────
    data object StockAdjustment : Screen("stock_adjustment?productId={productId}") {
        fun createRoute(productId: Long? = null): String {
            return if (productId != null) "stock_adjustment?productId=$productId"
            else "stock_adjustment"
        }
    }

    // ── Reports ─────────────────────────────────────────────────────────
    data object Reports : Screen("reports")
    data object SalesReport : Screen("reports/sales")
    data object PurchaseReport : Screen("reports/purchase")
    data object ProfitLossReport : Screen("reports/profit_loss")
    data object InventoryValuationReport : Screen("reports/inventory_valuation")
    data object CustomerLedgerReport : Screen("reports/customer_ledger")
    data object VendorPayableReport : Screen("reports/vendor_payable")
    data object LowStockReport : Screen("reports/low_stock")
    data object TopSellingReport : Screen("reports/top_selling")
    data object TaxReport : Screen("reports/tax")

    // ── Settings ────────────────────────────────────────────────────────
    data object Settings : Screen("settings")

    // ── Barcode Scanner ─────────────────────────────────────────────────
    data object BarcodeScanner : Screen("barcode_scanner")

    // ── CSV Import / Export ─────────────────────────────────────────────
    data object CsvImport : Screen("csv_import")
    data object CsvExport : Screen("csv_export")
}
