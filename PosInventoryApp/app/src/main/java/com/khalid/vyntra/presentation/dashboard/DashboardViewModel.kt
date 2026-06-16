package com.khalid.vyntra.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalid.vyntra.data.preferences.PreferencesManager
import com.khalid.vyntra.domain.model.Invoice
import com.khalid.vyntra.domain.model.InvoiceStatus
import com.khalid.vyntra.domain.model.Product
import com.khalid.vyntra.domain.repository.CustomerRepository
import com.khalid.vyntra.domain.repository.InvoiceRepository
import com.khalid.vyntra.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DashboardUiState(
    val businessName: String = "",
    val todaySales: Double = 0.0,
    val yesterdaySales: Double = 0.0,
    val monthlySales: Double = 0.0,
    val totalProducts: Int = 0,
    val lowStockCount: Int = 0,
    val totalOutstanding: Double = 0.0,
    /** Number of completed (non-cancelled) invoices created today. */
    val todayTransactionCount: Int = 0,
    /** Average value of today's completed invoices. */
    val todayAverageSale: Double = 0.0,
    /** Daily totals for the trailing 7 days (oldest → newest, ending today). */
    val last7DaysSales: List<Double> = emptyList(),
    val recentInvoices: List<Invoice> = emptyList(),
    val lowStockProducts: List<Product> = emptyList(),
    /** Currency symbol chosen by the user in Settings → Display. */
    val currencySymbol: String = "Rs",
    val isLoading: Boolean = true,
    val error: String? = null
) {
    /**
     * Day-over-day delta for today's revenue. `null` when there's no
     * baseline yet (first day of usage, or yesterday had zero revenue).
     */
    val todayDeltaPercent: Double?
        get() = if (yesterdaySales <= 0.0) null
                else (todaySales - yesterdaySales) / yesterdaySales * 100.0
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    /**
     * Snapshot of all the imperatively-loaded sales/aggregate values, kept
     * as a single flow so the outer combine stays at ≤ 5 args (the typed
     * combine overload).
     */
    private data class SalesSnapshot(
        val today: Double = 0.0,
        val yesterday: Double = 0.0,
        val monthly: Double = 0.0,
        val todayCount: Int = 0,
        val todayAverage: Double = 0.0,
        val last7Days: List<Double> = emptyList()
    )

    private data class CountsSnapshot(
        val totalProducts: Int = 0,
        val totalOutstanding: Double = 0.0
    )

    private val _sales = MutableStateFlow(SalesSnapshot())
    private val _counts = MutableStateFlow(CountsSnapshot())

    val uiState: StateFlow<DashboardUiState> = combine(
        _sales,
        _counts,
        productRepository.getLowStockProducts(),
        invoiceRepository.getAll(),
        combine(
            preferencesManager.businessName,
            preferencesManager.currencySymbol
        ) { name, sym -> name to sym }
    ) { sales, counts, lowStock, invoices, (businessName, currencySymbol) ->
        DashboardUiState(
            businessName = businessName,
            todaySales = sales.today,
            yesterdaySales = sales.yesterday,
            monthlySales = sales.monthly,
            totalProducts = counts.totalProducts,
            lowStockCount = lowStock.size,
            totalOutstanding = counts.totalOutstanding,
            todayTransactionCount = sales.todayCount,
            todayAverageSale = sales.todayAverage,
            last7DaysSales = sales.last7Days,
            recentInvoices = invoices.take(5),
            lowStockProducts = lowStock.take(10),
            currencySymbol = currencySymbol,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )

    init {
        loadSalesData()
        loadCounts()
    }

    private fun loadSalesData() {
        viewModelScope.launch {
            try {
                val now = Calendar.getInstance()
                val todayStart = startOfDay(now.timeInMillis)
                val yesterdayStart = startOfDay(todayStart - 1L)

                val today = invoiceRepository.getDailySales(todayStart)
                val yesterday = invoiceRepository.getDailySales(yesterdayStart)
                val monthly = invoiceRepository.getMonthlySales(
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH) + 1
                )

                // Trailing 7-day series (oldest → newest, ending today).
                val last7 = (6 downTo 0).map { offset ->
                    val dayStart = startOfDay(todayStart - offset * MILLIS_PER_DAY)
                    invoiceRepository.getDailySales(dayStart)
                }

                // Today's transaction count + average from today's invoices.
                val todayInvoices = invoiceRepository
                    .getByDateRange(todayStart, todayStart + MILLIS_PER_DAY)
                    .first()
                val completed = todayInvoices.filter { it.status != InvoiceStatus.CANCELLED }
                val avg = if (completed.isNotEmpty())
                    completed.sumOf { it.totalAmount } / completed.size
                else 0.0

                _sales.value = SalesSnapshot(
                    today = today,
                    yesterday = yesterday,
                    monthly = monthly,
                    todayCount = completed.size,
                    todayAverage = avg,
                    last7Days = last7
                )
            } catch (_: Exception) {
                // UI degrades to zero values.
            }
        }
    }

    private fun loadCounts() {
        viewModelScope.launch {
            try {
                _counts.value = _counts.value.copy(
                    totalProducts = productRepository.getProductCount()
                )
            } catch (_: Exception) { }
        }
        viewModelScope.launch {
            try {
                customerRepository.getCustomersWithOutstanding().collect { customers ->
                    _counts.value = _counts.value.copy(
                        totalOutstanding = customers.sumOf { it.outstandingBalance }
                    )
                }
            } catch (_: Exception) { }
        }
    }

    /** Re-runs sales aggregations + counts. Called from DashboardScreen on resume. */
    fun refresh() {
        loadSalesData()
        loadCounts()
    }

    private companion object {
        const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000

        fun startOfDay(epochMillis: Long): Long {
            val cal = Calendar.getInstance().apply {
                timeInMillis = epochMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }
    }
}
