package com.khalid.cashlytics.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalid.cashlytics.domain.model.*
import com.khalid.cashlytics.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ReportType {
    SALES, PURCHASE, PROFIT_LOSS, INVENTORY_VALUATION,
    CUSTOMER_LEDGER, VENDOR_PAYABLE, LOW_STOCK, TOP_SELLING, TAX
}

data class DateRange(val startDate: Long, val endDate: Long)

data class SalesReportData(
    val totalSales: Double = 0.0,
    val totalTransactions: Int = 0,
    val averageSale: Double = 0.0,
    val invoices: List<Invoice> = emptyList()
)

data class PurchaseReportData(
    val totalPurchases: Double = 0.0,
    val totalTransactions: Int = 0,
    val purchases: List<Purchase> = emptyList()
)

data class ProfitLossData(
    val totalSales: Double = 0.0,
    val totalPurchases: Double = 0.0,
    val grossProfit: Double = 0.0,
    val totalTax: Double = 0.0,
    val netProfit: Double = 0.0
)

data class InventoryValuationData(
    val totalItems: Int = 0,
    val totalValue: Double = 0.0,
    val products: List<Product> = emptyList()
)

data class ReportsUiState(
    val selectedReport: ReportType? = null,
    val dateRange: DateRange = DateRange(
        startDate = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000,
        endDate = System.currentTimeMillis()
    ),
    val salesData: SalesReportData? = null,
    val purchaseData: PurchaseReportData? = null,
    val profitLossData: ProfitLossData? = null,
    val inventoryValuationData: InventoryValuationData? = null,
    val customerLedger: List<Customer> = emptyList(),
    val vendorPayables: List<Vendor> = emptyList(),
    val lowStockProducts: List<Product> = emptyList(),
    val topSellingProducts: List<Pair<String, Int>> = emptyList(),
    val taxCollected: Double = 0.0,
    val isLoading: Boolean = false
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val purchaseRepository: PurchaseRepository,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val vendorRepository: VendorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    fun setDateRange(start: Long, end: Long) {
        _uiState.update { it.copy(dateRange = DateRange(start, end)) }
    }

    fun loadReport(type: ReportType) {
        _uiState.update { it.copy(selectedReport = type, isLoading = true) }
        viewModelScope.launch {
            when (type) {
                ReportType.SALES -> loadSalesReport()
                ReportType.PURCHASE -> loadPurchaseReport()
                ReportType.PROFIT_LOSS -> loadProfitLoss()
                ReportType.INVENTORY_VALUATION -> loadInventoryValuation()
                ReportType.CUSTOMER_LEDGER -> loadCustomerLedger()
                ReportType.VENDOR_PAYABLE -> loadVendorPayable()
                ReportType.LOW_STOCK -> loadLowStock()
                ReportType.TOP_SELLING -> loadTopSelling()
                ReportType.TAX -> loadTaxReport()
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun loadSalesReport() {
        val range = _uiState.value.dateRange
        invoiceRepository.getByDateRange(range.startDate, range.endDate).first().let { invoices ->
            val completed = invoices.filter { it.status == InvoiceStatus.COMPLETED || it.status == InvoiceStatus.CREDIT }
            val total = completed.sumOf { it.totalAmount }
            _uiState.update {
                it.copy(salesData = SalesReportData(
                    totalSales = total,
                    totalTransactions = completed.size,
                    averageSale = if (completed.isNotEmpty()) total / completed.size else 0.0,
                    invoices = completed
                ))
            }
        }
    }

    private suspend fun loadPurchaseReport() {
        val range = _uiState.value.dateRange
        purchaseRepository.getByDateRange(range.startDate, range.endDate).first().let { purchases ->
            _uiState.update {
                it.copy(purchaseData = PurchaseReportData(
                    totalPurchases = purchases.sumOf { p -> p.totalAmount },
                    totalTransactions = purchases.size,
                    purchases = purchases
                ))
            }
        }
    }

    private suspend fun loadProfitLoss() {
        val range = _uiState.value.dateRange
        val invoices = invoiceRepository.getByDateRange(range.startDate, range.endDate).first()
            .filter { it.status != InvoiceStatus.CANCELLED }
        val purchases = purchaseRepository.getByDateRange(range.startDate, range.endDate).first()
        val totalSales = invoices.sumOf { it.totalAmount }
        val totalTax = invoices.sumOf { it.taxAmount }
        val totalPurchases = purchases.sumOf { it.totalAmount }
        _uiState.update {
            it.copy(profitLossData = ProfitLossData(
                totalSales = totalSales,
                totalPurchases = totalPurchases,
                grossProfit = totalSales - totalPurchases,
                totalTax = totalTax,
                netProfit = totalSales - totalPurchases - totalTax
            ))
        }
    }

    private suspend fun loadInventoryValuation() {
        productRepository.getAllProducts().first().let { products ->
            val totalValue = products.sumOf { it.purchasePrice * it.currentStock }
            _uiState.update {
                it.copy(inventoryValuationData = InventoryValuationData(
                    totalItems = products.sumOf { p -> p.currentStock.toInt() },
                    totalValue = totalValue,
                    products = products
                ))
            }
        }
    }

    private suspend fun loadCustomerLedger() {
        customerRepository.getCustomersWithOutstanding().first().let { customers ->
            _uiState.update { it.copy(customerLedger = customers) }
        }
    }

    private suspend fun loadVendorPayable() {
        vendorRepository.getAll().first().let { vendors ->
            _uiState.update {
                it.copy(vendorPayables = vendors.filter { v -> v.currentPayableBalance > 0 })
            }
        }
    }

    private suspend fun loadLowStock() {
        productRepository.getLowStockProducts().first().let { products ->
            _uiState.update { it.copy(lowStockProducts = products) }
        }
    }

    private suspend fun loadTopSelling() {
        val range = _uiState.value.dateRange
        invoiceRepository.getTopSellingProducts(10, range.startDate, range.endDate).first().let { products ->
            _uiState.update { it.copy(topSellingProducts = products) }
        }
    }

    private suspend fun loadTaxReport() {
        val range = _uiState.value.dateRange
        invoiceRepository.getByDateRange(range.startDate, range.endDate).first().let { invoices ->
            val totalTax = invoices.filter { it.status != InvoiceStatus.CANCELLED }.sumOf { it.taxAmount }
            _uiState.update { it.copy(taxCollected = totalTax) }
        }
    }
}
