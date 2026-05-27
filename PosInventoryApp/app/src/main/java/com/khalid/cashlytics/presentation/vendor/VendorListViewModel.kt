package com.khalid.cashlytics.presentation.vendor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalid.cashlytics.domain.model.Vendor
import com.khalid.cashlytics.domain.repository.VendorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VendorListUiState(
    val vendors: List<Vendor> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

sealed interface VendorListEvent {
    data class ShowError(val message: String) : VendorListEvent
    data class VendorDeleted(val name: String) : VendorListEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class VendorListViewModel @Inject constructor(
    private val vendorRepository: VendorRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    private val _event = Channel<VendorListEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    private val filteredVendors = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            vendorRepository.getAll()
        } else {
            vendorRepository.search(query.trim())
        }
    }

    val uiState: StateFlow<VendorListUiState> = combine(
        filteredVendors,
        _searchQuery
    ) { vendors, query ->
        VendorListUiState(
            vendors = vendors,
            searchQuery = query,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VendorListUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun deleteVendor(vendor: Vendor) {
        viewModelScope.launch {
            try {
                vendorRepository.delete(vendor.id)
                _event.send(VendorListEvent.VendorDeleted(vendor.name))
            } catch (e: Exception) {
                _event.send(VendorListEvent.ShowError(e.message ?: "Failed to delete vendor"))
            }
        }
    }
}
