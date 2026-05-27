package com.khalid.cashlytics.presentation.customer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalid.cashlytics.domain.model.Customer
import com.khalid.cashlytics.domain.model.EntityType
import com.khalid.cashlytics.domain.model.Invoice
import com.khalid.cashlytics.domain.model.PaymentMethod
import com.khalid.cashlytics.domain.model.PaymentRecord
import com.khalid.cashlytics.domain.repository.CustomerRepository
import com.khalid.cashlytics.domain.repository.InvoiceRepository
import com.khalid.cashlytics.domain.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerDetailUiState(
    val customer: Customer? = null,
    val invoices: List<Invoice> = emptyList(),
    val payments: List<PaymentRecord> = emptyList(),
    val outstandingBalance: Double = 0.0,
    val isLoading: Boolean = true,
    val paymentDialogState: PaymentDialogState = PaymentDialogState.Hidden
)

sealed interface PaymentDialogState {
    data object Hidden : PaymentDialogState
    data class Shown(
        val amount: String = "",
        val paymentMethod: PaymentMethod = PaymentMethod.CASH,
        val notes: String = "",
        val amountError: String? = null
    ) : PaymentDialogState
}

sealed interface CustomerDetailEvent {
    data class ShowError(val message: String) : CustomerDetailEvent
    data object PaymentRecorded : CustomerDetailEvent
}

@HiltViewModel
class CustomerDetailViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val invoiceRepository: InvoiceRepository,
    private val paymentRepository: PaymentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val customerId: Long = savedStateHandle.get<Long>("customerId") ?: 0L

    private val _paymentDialogState = MutableStateFlow<PaymentDialogState>(PaymentDialogState.Hidden)

    private val _event = Channel<CustomerDetailEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    private val _customerState = MutableStateFlow<Customer?>(null)

    val uiState: StateFlow<CustomerDetailUiState> = combine(
        _customerState,
        invoiceRepository.getByCustomer(customerId),
        paymentRepository.getByEntity(EntityType.CUSTOMER, customerId),
        _paymentDialogState
    ) { customer, invoices, payments, dialogState ->
        CustomerDetailUiState(
            customer = customer,
            invoices = invoices,
            payments = payments,
            outstandingBalance = customer?.outstandingBalance ?: 0.0,
            isLoading = customer == null,
            paymentDialogState = dialogState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CustomerDetailUiState()
    )

    init {
        loadCustomer()
    }

    private fun loadCustomer() {
        viewModelScope.launch {
            try {
                val customer = customerRepository.getById(customerId)
                _customerState.value = customer
                if (customer == null) {
                    _event.send(CustomerDetailEvent.ShowError("Customer not found"))
                }
            } catch (e: Exception) {
                _event.send(CustomerDetailEvent.ShowError(e.message ?: "Failed to load customer"))
            }
        }
    }

    fun showPaymentDialog() {
        _paymentDialogState.value = PaymentDialogState.Shown()
    }

    fun dismissPaymentDialog() {
        _paymentDialogState.value = PaymentDialogState.Hidden
    }

    fun onPaymentAmountChanged(amount: String) {
        val current = _paymentDialogState.value
        if (current is PaymentDialogState.Shown) {
            _paymentDialogState.value = current.copy(amount = amount, amountError = null)
        }
    }

    fun onPaymentMethodChanged(method: PaymentMethod) {
        val current = _paymentDialogState.value
        if (current is PaymentDialogState.Shown) {
            _paymentDialogState.value = current.copy(paymentMethod = method)
        }
    }

    fun onPaymentNotesChanged(notes: String) {
        val current = _paymentDialogState.value
        if (current is PaymentDialogState.Shown) {
            _paymentDialogState.value = current.copy(notes = notes)
        }
    }

    fun recordPayment() {
        val dialog = _paymentDialogState.value
        if (dialog !is PaymentDialogState.Shown) return

        val amount = dialog.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _paymentDialogState.value = dialog.copy(amountError = "Enter a valid amount greater than 0")
            return
        }

        val customer = _customerState.value ?: return

        viewModelScope.launch {
            try {
                val payment = PaymentRecord(
                    entityType = EntityType.CUSTOMER,
                    entityId = customerId,
                    entityName = customer.name,
                    amount = amount,
                    paymentMethod = dialog.paymentMethod,
                    notes = dialog.notes.trim().ifBlank { null }
                )

                paymentRepository.addPayment(payment)

                val newBalance = (customer.outstandingBalance - amount).coerceAtLeast(0.0)
                customerRepository.updateOutstandingBalance(customerId, newBalance)

                _customerState.value = customer.copy(outstandingBalance = newBalance)
                _paymentDialogState.value = PaymentDialogState.Hidden
                _event.send(CustomerDetailEvent.PaymentRecorded)
            } catch (e: Exception) {
                _event.send(CustomerDetailEvent.ShowError(e.message ?: "Failed to record payment"))
            }
        }
    }
}
