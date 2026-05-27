package com.khalid.cashlytics.presentation.stock

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalid.cashlytics.domain.model.AdjustmentType
import com.khalid.cashlytics.domain.model.Product
import com.khalid.cashlytics.domain.model.StockAdjustment
import com.khalid.cashlytics.domain.repository.ProductRepository
import com.khalid.cashlytics.domain.repository.StockAdjustmentRepository
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

data class StockAdjustmentUiState(
    val product: Product? = null,
    val currentStock: Double = 0.0,
    val newStock: String = "",
    val reason: String = "",
    val adjustmentType: AdjustmentType = AdjustmentType.MANUAL,
    val history: List<StockAdjustment> = emptyList(),
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val error: String? = null
)

sealed interface StockAdjustmentEvent {
    data class ShowError(val message: String) : StockAdjustmentEvent
    data object AdjustmentSaved : StockAdjustmentEvent
}

@HiltViewModel
class StockAdjustmentViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val stockAdjustmentRepository: StockAdjustmentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId: Long = savedStateHandle.get<Long>("productId") ?: 0L

    private val _product = MutableStateFlow<Product?>(null)
    private val _newStock = MutableStateFlow("")
    private val _reason = MutableStateFlow("")
    private val _adjustmentType = MutableStateFlow(AdjustmentType.MANUAL)
    private val _isSaved = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    private val _event = Channel<StockAdjustmentEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    val uiState: StateFlow<StockAdjustmentUiState> = combine(
        _product,
        _newStock,
        _reason,
        _adjustmentType,
        combine(
            stockAdjustmentRepository.getByProduct(productId),
            _isSaved,
            _isLoading,
            _error
        ) { history, isSaved, isLoading, error ->
            Triple(history, isSaved, isLoading to error)
        }
    ) { product, newStock, reason, adjustmentType, (history, isSaved, loadingError) ->
        StockAdjustmentUiState(
            product = product,
            currentStock = product?.currentStock ?: 0.0,
            newStock = newStock,
            reason = reason,
            adjustmentType = adjustmentType,
            history = history,
            isLoading = loadingError.first,
            isSaved = isSaved,
            error = loadingError.second
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StockAdjustmentUiState()
    )

    init {
        if (productId != 0L) {
            loadProduct(productId)
        }
    }

    fun loadProduct(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val product = productRepository.getProductById(id)
                if (product != null) {
                    _product.value = product
                    _newStock.value = product.currentStock.toString()
                } else {
                    _error.value = "Product not found"
                    _event.send(StockAdjustmentEvent.ShowError("Product not found"))
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load product"
                _event.send(StockAdjustmentEvent.ShowError(e.message ?: "Failed to load product"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateNewStock(value: String) {
        _newStock.value = value
    }

    fun setReason(reason: String) {
        _reason.value = reason
    }

    fun setAdjustmentType(type: AdjustmentType) {
        _adjustmentType.value = type
    }

    fun saveAdjustment() {
        val product = _product.value ?: return

        val newStockValue = _newStock.value.toDoubleOrNull()
        if (newStockValue == null || newStockValue < 0) {
            viewModelScope.launch {
                _event.send(StockAdjustmentEvent.ShowError("Enter a valid stock quantity (>= 0)"))
            }
            return
        }

        val adjustmentQty = newStockValue - product.currentStock
        if (adjustmentQty == 0.0) {
            viewModelScope.launch {
                _event.send(StockAdjustmentEvent.ShowError("New stock is the same as current stock"))
            }
            return
        }

        viewModelScope.launch {
            try {
                val adjustment = StockAdjustment(
                    productId = product.id,
                    productName = product.name,
                    previousStock = product.currentStock,
                    newStock = newStockValue,
                    adjustmentQuantity = adjustmentQty,
                    reason = _reason.value.trim().ifBlank { null },
                    type = _adjustmentType.value
                )

                stockAdjustmentRepository.addAdjustment(adjustment)
                productRepository.updateStock(product.id, newStockValue)

                _product.value = product.copy(
                    currentStock = newStockValue,
                    updatedAt = System.currentTimeMillis()
                )
                _isSaved.value = true
                _event.send(StockAdjustmentEvent.AdjustmentSaved)
            } catch (e: Exception) {
                _event.send(StockAdjustmentEvent.ShowError(e.message ?: "Failed to save adjustment"))
            }
        }
    }
}
