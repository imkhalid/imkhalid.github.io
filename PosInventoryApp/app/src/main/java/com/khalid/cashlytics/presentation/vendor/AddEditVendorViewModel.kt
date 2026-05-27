package com.khalid.cashlytics.presentation.vendor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalid.cashlytics.domain.model.Vendor
import com.khalid.cashlytics.domain.repository.VendorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditVendorUiState(
    val isEditMode: Boolean = false,
    val vendorId: Long = 0,
    val name: String = "",
    val company: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val bankDetails: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val validationErrors: Map<String, String> = emptyMap()
)

sealed interface AddEditVendorEvent {
    data object SaveSuccess : AddEditVendorEvent
    data class ShowError(val message: String) : AddEditVendorEvent
}

@HiltViewModel
class AddEditVendorViewModel @Inject constructor(
    private val vendorRepository: VendorRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vendorId: Long = savedStateHandle.get<Long>("vendorId") ?: 0L

    private val _uiState = MutableStateFlow(
        AddEditVendorUiState(isEditMode = vendorId > 0, vendorId = vendorId)
    )
    val uiState: StateFlow<AddEditVendorUiState> = _uiState.asStateFlow()

    private val _event = Channel<AddEditVendorEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        if (vendorId > 0) {
            loadVendor(vendorId)
        }
    }

    private fun loadVendor(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val vendor = vendorRepository.getById(id)
                if (vendor != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = vendor.name,
                            company = vendor.company ?: "",
                            phone = vendor.phone ?: "",
                            email = vendor.email ?: "",
                            address = vendor.address ?: "",
                            bankDetails = vendor.bankDetails ?: "",
                            notes = vendor.notes ?: ""
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    _event.send(AddEditVendorEvent.ShowError("Vendor not found"))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _event.send(AddEditVendorEvent.ShowError(e.message ?: "Failed to load vendor"))
            }
        }
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value, validationErrors = it.validationErrors - "name") }
    }

    fun onCompanyChanged(value: String) {
        _uiState.update { it.copy(company = value) }
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

    fun onBankDetailsChanged(value: String) {
        _uiState.update { it.copy(bankDetails = value) }
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
                val vendor = Vendor(
                    id = if (state.isEditMode) state.vendorId else 0,
                    name = state.name.trim(),
                    company = state.company.trim().ifBlank { null },
                    phone = state.phone.trim().ifBlank { null },
                    email = state.email.trim().ifBlank { null },
                    address = state.address.trim().ifBlank { null },
                    bankDetails = state.bankDetails.trim().ifBlank { null },
                    notes = state.notes.trim().ifBlank { null }
                )

                if (state.isEditMode) {
                    vendorRepository.update(vendor)
                } else {
                    vendorRepository.add(vendor)
                }

                _uiState.update { it.copy(isSaving = false) }
                _event.send(AddEditVendorEvent.SaveSuccess)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                _event.send(AddEditVendorEvent.ShowError(e.message ?: "Failed to save vendor"))
            }
        }
    }

    private fun validate(state: AddEditVendorUiState): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (state.name.isBlank()) {
            errors["name"] = "Vendor name is required"
        }
        return errors
    }
}
