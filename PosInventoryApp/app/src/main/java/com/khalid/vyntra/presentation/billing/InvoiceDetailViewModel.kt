package com.khalid.vyntra.presentation.billing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalid.vyntra.data.preferences.PreferencesManager
import com.khalid.vyntra.domain.model.AdjustmentType
import com.khalid.vyntra.domain.model.Invoice
import com.khalid.vyntra.domain.model.InvoiceStatus
import com.khalid.vyntra.domain.model.StockAdjustment
import com.khalid.vyntra.domain.repository.CustomerRepository
import com.khalid.vyntra.domain.repository.InvoiceRepository
import com.khalid.vyntra.domain.repository.ProductRepository
import com.khalid.vyntra.domain.repository.StockAdjustmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Snapshot of the business profile needed by the PDF header. Cached
 * alongside the invoice so the Share / Print buttons don't have to
 * round-trip DataStore on each tap.
 */
data class BusinessProfileSnapshot(
    val name: String = "",
    val address: String = "",
    val taxNumber: String = "",
    val currencySymbol: String = "Rs"
)

data class InvoiceDetailUiState(
    val invoice: Invoice? = null,
    val business: BusinessProfileSnapshot = BusinessProfileSnapshot(),
    val isLoading: Boolean = true,
    val isCancelling: Boolean = false
)

sealed interface InvoiceDetailEvent {
    data class ShowError(val message: String) : InvoiceDetailEvent
    data object InvoiceCancelled : InvoiceDetailEvent
}

@HiltViewModel
class InvoiceDetailViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val stockAdjustmentRepository: StockAdjustmentRepository,
    private val preferencesManager: PreferencesManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val invoiceId: Long = savedStateHandle.get<Long>("invoiceId") ?: 0L

    private val _uiState = MutableStateFlow(InvoiceDetailUiState())
    val uiState: StateFlow<InvoiceDetailUiState> = _uiState.asStateFlow()

    private val _event = Channel<InvoiceDetailEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        loadInvoice()
        loadBusinessProfile()
    }

    /**
     * One-shot read of the business profile. Reload on demand is unnecessary
     * — the profile rarely changes and we just need a snapshot when the
     * user taps Share / Print.
     */
    private fun loadBusinessProfile() {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        business = BusinessProfileSnapshot(
                            name = preferencesManager.businessName.first(),
                            address = preferencesManager.businessAddress.first(),
                            taxNumber = preferencesManager.taxNumber.first(),
                            currencySymbol = preferencesManager.currencySymbol.first()
                        )
                    )
                }
            } catch (_: Exception) {
                // PDF header will fall back to generic labels.
            }
        }
    }

    private fun loadInvoice() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val invoice = invoiceRepository.getById(invoiceId)
                _uiState.update { it.copy(invoice = invoice, isLoading = false) }
                if (invoice == null) {
                    _event.send(InvoiceDetailEvent.ShowError("Invoice not found"))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _event.send(InvoiceDetailEvent.ShowError(e.message ?: "Failed to load invoice"))
            }
        }
    }

    fun cancelInvoice() {
        val invoice = _uiState.value.invoice ?: return

        if (invoice.status == InvoiceStatus.CANCELLED) {
            viewModelScope.launch {
                _event.send(InvoiceDetailEvent.ShowError("Invoice is already cancelled"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true) }
            try {
                // Reverse stock for each item
                for (item in invoice.items) {
                    val product = productRepository.getProductById(item.productId)
                    if (product != null) {
                        val restoredStock = product.currentStock + item.quantity
                        productRepository.updateStock(item.productId, restoredStock)

                        stockAdjustmentRepository.addAdjustment(
                            StockAdjustment(
                                productId = item.productId,
                                productName = item.productName,
                                previousStock = product.currentStock,
                                newStock = restoredStock,
                                adjustmentQuantity = item.quantity,
                                reason = "Cancelled - Invoice ${invoice.invoiceNumber}",
                                type = AdjustmentType.RETURN
                            )
                        )
                    }
                }

                // Reverse customer outstanding balance if it was a credit sale
                if (invoice.status == InvoiceStatus.CREDIT && invoice.customerId != null) {
                    val unpaidAmount = invoice.totalAmount - invoice.paidAmount
                    if (unpaidAmount > 0) {
                        val customer = customerRepository.getById(invoice.customerId)
                        if (customer != null) {
                            val newBalance = (customer.outstandingBalance - unpaidAmount).coerceAtLeast(0.0)
                            customerRepository.updateOutstandingBalance(
                                invoice.customerId,
                                newBalance
                            )
                        }
                    }
                }

                invoiceRepository.cancelInvoice(invoiceId)

                // Reload the invoice to reflect the updated status
                val updated = invoiceRepository.getById(invoiceId)
                _uiState.update { it.copy(invoice = updated, isCancelling = false) }
                _event.send(InvoiceDetailEvent.InvoiceCancelled)
            } catch (e: Exception) {
                _uiState.update { it.copy(isCancelling = false) }
                _event.send(
                    InvoiceDetailEvent.ShowError(e.message ?: "Failed to cancel invoice")
                )
            }
        }
    }
}
