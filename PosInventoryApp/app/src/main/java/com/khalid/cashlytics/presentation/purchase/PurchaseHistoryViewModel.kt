package com.khalid.cashlytics.presentation.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalid.cashlytics.domain.model.Purchase
import com.khalid.cashlytics.domain.model.Vendor
import com.khalid.cashlytics.domain.repository.PurchaseRepository
import com.khalid.cashlytics.domain.repository.VendorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class PurchaseHistoryUiState(
    val purchases: List<Purchase> = emptyList(),
    val vendors: List<Vendor> = emptyList(),
    val selectedVendorId: Long? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PurchaseHistoryViewModel @Inject constructor(
    private val purchaseRepository: PurchaseRepository,
    private val vendorRepository: VendorRepository
) : ViewModel() {

    private val _selectedVendorId = MutableStateFlow<Long?>(null)
    private val _startDate = MutableStateFlow<Long?>(null)
    private val _endDate = MutableStateFlow<Long?>(null)

    private val filteredPurchases = combine(
        _selectedVendorId,
        _startDate,
        _endDate
    ) { vendorId, start, end ->
        Triple(vendorId, start, end)
    }.flatMapLatest { (vendorId, start, end) ->
        when {
            vendorId != null -> purchaseRepository.getByVendor(vendorId)
            start != null && end != null -> purchaseRepository.getByDateRange(start, end)
            else -> purchaseRepository.getAll()
        }
    }

    val uiState: StateFlow<PurchaseHistoryUiState> = combine(
        filteredPurchases,
        vendorRepository.getAll(),
        _selectedVendorId,
        _startDate,
        _endDate
    ) { purchases, vendors, vendorId, start, end ->
        PurchaseHistoryUiState(
            purchases = purchases,
            vendors = vendors,
            selectedVendorId = vendorId,
            startDate = start,
            endDate = end,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PurchaseHistoryUiState()
    )

    fun onVendorFilterChanged(vendorId: Long?) {
        _selectedVendorId.value = vendorId
    }

    fun onDateRangeChanged(startDate: Long?, endDate: Long?) {
        _startDate.value = startDate
        _endDate.value = endDate
    }

    fun clearFilters() {
        _selectedVendorId.value = null
        _startDate.value = null
        _endDate.value = null
    }
}
