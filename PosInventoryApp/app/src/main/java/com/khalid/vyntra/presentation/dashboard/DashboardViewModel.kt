package com.khalid.vyntra.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalid.vyntra.domain.model.Invoice
import com.khalid.vyntra.domain.model.Product
import com.khalid.vyntra.domain.repository.CustomerRepository
import com.khalid.vyntra.domain.repository.InvoiceRepository
import com.khalid.vyntra.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DashboardUiState(
    val todaySales: Double = 0.0,
    val monthlySales: Double = 0.0,
    val totalProducts: Int = 0,
    val lowStockCount: Int = 0,
    val totalOutstanding: Double = 0.0,
    val recentInvoices: List<Invoice> = emptyList(),
    val lowStockProducts: List<Product> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _salesData = MutableStateFlow(Pair(0.0, 0.0)) // today, monthly
    private val _totalProducts = MutableStateFlow(0)
    private val _totalOutstanding = MutableStateFlow(0.0)

    val uiState: StateFlow<DashboardUiState> = combine(
        _salesData,
        productRepository.getLowStockProducts(),
        invoiceRepository.getAll(),
        _totalProducts,
        _totalOutstanding
    ) { salesData, lowStock, allInvoices, productCount, outstanding ->
        DashboardUiState(
            todaySales = salesData.first,
            monthlySales = salesData.second,
            totalProducts = productCount,
            lowStockCount = lowStock.size,
            totalOutstanding = outstanding,
            recentInvoices = allInvoices.take(5),
            lowStockProducts = lowStock.take(10),
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
                val calendar = Calendar.getInstance()
                val todayStart = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val today = invoiceRepository.getDailySales(todayStart)

                val now = Calendar.getInstance()
                val monthly = invoiceRepository.getMonthlySales(
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH) + 1
                )

                _salesData.value = Pair(today, monthly)
            } catch (_: Exception) {
                // Sales data will remain at 0.0
            }
        }
    }

    private fun loadCounts() {
        viewModelScope.launch {
            try {
                _totalProducts.value = productRepository.getProductCount()
            } catch (_: Exception) { }
        }
        viewModelScope.launch {
            try {
                customerRepository.getCustomersWithOutstanding().collect { customers ->
                    _totalOutstanding.value = customers.sumOf { it.outstandingBalance }
                }
            } catch (_: Exception) { }
        }
    }

    fun refresh() {
        loadSalesData()
        loadCounts()
    }
}
