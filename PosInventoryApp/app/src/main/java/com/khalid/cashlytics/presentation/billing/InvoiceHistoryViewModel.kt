package com.khalid.cashlytics.presentation.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalid.cashlytics.domain.model.Invoice
import com.khalid.cashlytics.domain.model.InvoiceStatus
import com.khalid.cashlytics.domain.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvoiceHistoryUiState(
    val invoices: List<Invoice> = emptyList(),
    val searchQuery: String = "",
    val statusFilter: InvoiceStatus? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val isLoading: Boolean = true
)

sealed interface InvoiceHistoryEvent {
    data class ShowError(val message: String) : InvoiceHistoryEvent
    data class InvoiceCancelled(val invoiceNumber: String) : InvoiceHistoryEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InvoiceHistoryViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow<InvoiceStatus?>(null)
    private val _startDate = MutableStateFlow<Long?>(null)
    private val _endDate = MutableStateFlow<Long?>(null)

    private val _event = Channel<InvoiceHistoryEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    private val filteredInvoices = combine(
        _statusFilter,
        _startDate,
        _endDate
    ) { status, start, end ->
        Triple(status, start, end)
    }.flatMapLatest { (status, start, end) ->
        when {
            status != null -> invoiceRepository.getByStatus(status)
            start != null && end != null -> invoiceRepository.getByDateRange(start, end)
            else -> invoiceRepository.getAll()
        }
    }

    val uiState: StateFlow<InvoiceHistoryUiState> = combine(
        filteredInvoices,
        _searchQuery,
        _statusFilter,
        _startDate,
        _endDate
    ) { invoices, query, status, start, end ->
        val displayedInvoices = if (query.isBlank()) {
            invoices
        } else {
            invoices.filter { invoice ->
                invoice.invoiceNumber.contains(query, ignoreCase = true) ||
                    invoice.customerName?.contains(query, ignoreCase = true) == true
            }
        }

        InvoiceHistoryUiState(
            invoices = displayedInvoices,
            searchQuery = query,
            statusFilter = status,
            startDate = start,
            endDate = end,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InvoiceHistoryUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onStatusFilterChanged(status: InvoiceStatus?) {
        _statusFilter.value = status
    }

    fun onDateRangeChanged(startDate: Long?, endDate: Long?) {
        _startDate.value = startDate
        _endDate.value = endDate
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _statusFilter.value = null
        _startDate.value = null
        _endDate.value = null
    }

    fun cancelInvoice(invoiceId: Long) {
        viewModelScope.launch {
            try {
                val invoice = invoiceRepository.getById(invoiceId)
                    ?: throw IllegalArgumentException("Invoice not found")

                if (invoice.status == InvoiceStatus.CANCELLED) {
                    _event.send(InvoiceHistoryEvent.ShowError("Invoice is already cancelled"))
                    return@launch
                }

                invoiceRepository.cancelInvoice(invoiceId)
                _event.send(InvoiceHistoryEvent.InvoiceCancelled(invoice.invoiceNumber))
            } catch (e: Exception) {
                _event.send(
                    InvoiceHistoryEvent.ShowError(e.message ?: "Failed to cancel invoice")
                )
            }
        }
    }
}
