package com.khalid.vyntra.presentation.vendor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalid.vyntra.domain.model.EntityType
import com.khalid.vyntra.domain.model.PaymentMethod
import com.khalid.vyntra.domain.model.PaymentRecord
import com.khalid.vyntra.domain.model.Purchase
import com.khalid.vyntra.domain.model.Vendor
import com.khalid.vyntra.domain.repository.PaymentRepository
import com.khalid.vyntra.domain.repository.PurchaseRepository
import com.khalid.vyntra.domain.repository.VendorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VendorDetailUiState(
    val vendor: Vendor? = null,
    val purchases: List<Purchase> = emptyList(),
    val payments: List<PaymentRecord> = emptyList(),
    val payableBalance: Double = 0.0,
    val isLoading: Boolean = true,
    val paymentDialogState: VendorPaymentDialogState = VendorPaymentDialogState.Hidden
)

sealed interface VendorPaymentDialogState {
    data object Hidden : VendorPaymentDialogState
    data class Shown(
        val amount: String = "",
        val paymentMethod: PaymentMethod = PaymentMethod.CASH,
        val notes: String = "",
        val amountError: String? = null
    ) : VendorPaymentDialogState
}

sealed interface VendorDetailEvent {
    data class ShowError(val message: String) : VendorDetailEvent
    data object PaymentRecorded : VendorDetailEvent
}

@HiltViewModel
class VendorDetailViewModel @Inject constructor(
    private val vendorRepository: VendorRepository,
    private val purchaseRepository: PurchaseRepository,
    private val paymentRepository: PaymentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vendorId: Long = savedStateHandle.get<Long>("vendorId") ?: 0L

    private val _paymentDialogState = MutableStateFlow<VendorPaymentDialogState>(VendorPaymentDialogState.Hidden)

    private val _event = Channel<VendorDetailEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    private val _vendorState = MutableStateFlow<Vendor?>(null)

    val uiState: StateFlow<VendorDetailUiState> = combine(
        _vendorState,
        purchaseRepository.getByVendor(vendorId),
        paymentRepository.getByEntity(EntityType.VENDOR, vendorId),
        _paymentDialogState
    ) { vendor, purchases, payments, dialogState ->
        VendorDetailUiState(
            vendor = vendor,
            purchases = purchases,
            payments = payments,
            payableBalance = vendor?.currentPayableBalance ?: 0.0,
            isLoading = vendor == null,
            paymentDialogState = dialogState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VendorDetailUiState()
    )

    init {
        loadVendor()
    }

    private fun loadVendor() {
        viewModelScope.launch {
            try {
                val vendor = vendorRepository.getById(vendorId)
                _vendorState.value = vendor
                if (vendor == null) {
                    _event.send(VendorDetailEvent.ShowError("Vendor not found"))
                }
            } catch (e: Exception) {
                _event.send(VendorDetailEvent.ShowError(e.message ?: "Failed to load vendor"))
            }
        }
    }

    fun showPaymentDialog() {
        _paymentDialogState.value = VendorPaymentDialogState.Shown()
    }

    fun dismissPaymentDialog() {
        _paymentDialogState.value = VendorPaymentDialogState.Hidden
    }

    fun onPaymentAmountChanged(amount: String) {
        val current = _paymentDialogState.value
        if (current is VendorPaymentDialogState.Shown) {
            _paymentDialogState.value = current.copy(amount = amount, amountError = null)
        }
    }

    fun onPaymentMethodChanged(method: PaymentMethod) {
        val current = _paymentDialogState.value
        if (current is VendorPaymentDialogState.Shown) {
            _paymentDialogState.value = current.copy(paymentMethod = method)
        }
    }

    fun onPaymentNotesChanged(notes: String) {
        val current = _paymentDialogState.value
        if (current is VendorPaymentDialogState.Shown) {
            _paymentDialogState.value = current.copy(notes = notes)
        }
    }

    fun recordPayment() {
        val dialog = _paymentDialogState.value
        if (dialog !is VendorPaymentDialogState.Shown) return

        val amount = dialog.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _paymentDialogState.value = dialog.copy(amountError = "Enter a valid amount greater than 0")
            return
        }

        val vendor = _vendorState.value ?: return

        viewModelScope.launch {
            try {
                val payment = PaymentRecord(
                    entityType = EntityType.VENDOR,
                    entityId = vendorId,
                    entityName = vendor.name,
                    amount = amount,
                    paymentMethod = dialog.paymentMethod,
                    notes = dialog.notes.trim().ifBlank { null }
                )

                paymentRepository.addPayment(payment)

                val newBalance = (vendor.currentPayableBalance - amount).coerceAtLeast(0.0)
                vendorRepository.updatePayableBalance(vendorId, newBalance)

                _vendorState.value = vendor.copy(currentPayableBalance = newBalance)
                _paymentDialogState.value = VendorPaymentDialogState.Hidden
                _event.send(VendorDetailEvent.PaymentRecorded)
            } catch (e: Exception) {
                _event.send(VendorDetailEvent.ShowError(e.message ?: "Failed to record payment"))
            }
        }
    }
}
