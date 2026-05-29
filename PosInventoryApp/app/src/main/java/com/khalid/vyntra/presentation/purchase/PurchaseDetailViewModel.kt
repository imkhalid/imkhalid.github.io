package com.khalid.vyntra.presentation.purchase

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalid.vyntra.domain.model.Purchase
import com.khalid.vyntra.domain.repository.PurchaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PurchaseDetailUiState(
    val purchase: Purchase? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class PurchaseDetailViewModel @Inject constructor(
    private val purchaseRepository: PurchaseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val purchaseId: Long = savedStateHandle.get<Long>("purchaseId") ?: -1L

    private val _uiState = MutableStateFlow(PurchaseDetailUiState())
    val uiState: StateFlow<PurchaseDetailUiState> = _uiState.asStateFlow()

    init {
        if (purchaseId > 0) loadPurchase()
    }

    private fun loadPurchase() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val purchase = purchaseRepository.getById(purchaseId)
            _uiState.update { it.copy(purchase = purchase, isLoading = false) }
        }
    }
}
