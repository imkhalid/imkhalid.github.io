package com.khalid.cashlytics.presentation.customer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalid.cashlytics.domain.model.Customer
import com.khalid.cashlytics.domain.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditCustomerUiState(
    val isEditMode: Boolean = false,
    val customerId: Long = 0,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val cnicOrId: String = "",
    val creditLimit: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val validationErrors: Map<String, String> = emptyMap()
)

sealed interface AddEditCustomerEvent {
    data object SaveSuccess : AddEditCustomerEvent
    data class ShowError(val message: String) : AddEditCustomerEvent
}

@HiltViewModel
class AddEditCustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val customerId: Long = savedStateHandle.get<Long>("customerId") ?: 0L

    private val _uiState = MutableStateFlow(
        AddEditCustomerUiState(isEditMode = customerId > 0, customerId = customerId)
    )
    val uiState: StateFlow<AddEditCustomerUiState> = _uiState.asStateFlow()

    private val _event = Channel<AddEditCustomerEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        if (customerId > 0) {
            loadCustomer(customerId)
        }
    }

    private fun loadCustomer(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val customer = customerRepository.getById(id)
                if (customer != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = customer.name,
                            phone = customer.phone ?: "",
                            email = customer.email ?: "",
                            address = customer.address ?: "",
                            cnicOrId = customer.cnicOrId ?: "",
                            creditLimit = if (customer.creditLimit > 0)
                                customer.creditLimit.toBigDecimal().stripTrailingZeros().toPlainString()
                            else "",
                            notes = customer.notes ?: ""
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    _event.send(AddEditCustomerEvent.ShowError("Customer not found"))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _event.send(AddEditCustomerEvent.ShowError(e.message ?: "Failed to load customer"))
            }
        }
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value, validationErrors = it.validationErrors - "name") }
    }

    fun onPhoneChanged(value: String) {
        _uiState.update { it.copy(phone = value) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value) }
    }

    fun onAddressChanged(value: String) {
        _uiState.update { it.copy(address = value) }
    }

    fun onCnicOrIdChanged(value: String) {
        _uiState.update { it.copy(cnicOrId = value) }
    }

    fun onCreditLimitChanged(value: String) {
        _uiState.update { it.copy(creditLimit = value) }
    }

    fun onNotesChanged(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    fun save() {
        val state = _uiState.value
        val errors = validate(state)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(validationErrors = errors) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val customer = Customer(
                    id = if (state.isEditMode) state.customerId else 0,
                    name = state.name.trim(),
                    phone = state.phone.trim().ifBlank { null },
                    email = state.email.trim().ifBlank { null },
                    address = state.address.trim().ifBlank { null },
                    cnicOrId = state.cnicOrId.trim().ifBlank { null },
                    creditLimit = state.creditLimit.toDoubleOrNull() ?: 0.0,
                    notes = state.notes.trim().ifBlank { null }
                )

                if (state.isEditMode) {
                    customerRepository.update(customer)
                } else {
                    customerRepository.add(customer)
                }

                _uiState.update { it.copy(isSaving = false) }
                _event.send(AddEditCustomerEvent.SaveSuccess)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                _event.send(AddEditCustomerEvent.ShowError(e.message ?: "Failed to save customer"))
            }
        }
    }

    private fun validate(state: AddEditCustomerUiState): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (state.name.isBlank()) {
            errors["name"] = "Customer name is required"
        }
        return errors
    }
}
