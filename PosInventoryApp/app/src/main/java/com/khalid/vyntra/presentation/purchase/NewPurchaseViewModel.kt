package com.khalid.vyntra.presentation.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalid.vyntra.domain.model.AdjustmentType
import com.khalid.vyntra.domain.model.Product
import com.khalid.vyntra.domain.model.Purchase
import com.khalid.vyntra.domain.model.PurchaseItem
import com.khalid.vyntra.domain.model.StockAdjustment
import com.khalid.vyntra.domain.model.Vendor
import com.khalid.vyntra.domain.repository.ProductRepository
import com.khalid.vyntra.domain.repository.PurchaseRepository
import com.khalid.vyntra.domain.repository.StockAdjustmentRepository
import com.khalid.vyntra.domain.repository.VendorRepository
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

data class PurchaseCartItem(
    val product: Product,
    val quantity: Double = 1.0,
    val costPrice: Double = 0.0
) {
    val lineTotal: Double get() = costPrice * quantity
}

data class NewPurchaseUiState(
    val items: List<PurchaseCartItem> = emptyList(),
    val selectedVendor: Vendor? = null,
    val total: Double = 0.0,
    val notes: String = "",
    val searchQuery: String = "",
    val searchResults: List<Product> = emptyList(),
    val isProcessing: Boolean = false
)

sealed interface NewPurchaseEvent {
    data class ShowError(val message: String) : NewPurchaseEvent
    data class PurchaseCompleted(val purchaseId: Long) : NewPurchaseEvent
}

@HiltViewModel
class NewPurchaseViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val vendorRepository: VendorRepository,
    private val purchaseRepository: PurchaseRepository,
    private val stockAdjustmentRepository: StockAdjustmentRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<PurchaseCartItem>>(emptyList())
    private val _selectedVendor = MutableStateFlow<Vendor?>(null)
    private val _notes = MutableStateFlow("")
    private val _searchQuery = MutableStateFlow("")
    private val _searchResults = MutableStateFlow<List<Product>>(emptyList())
    private val _isProcessing = MutableStateFlow(false)

    private val _event = Channel<NewPurchaseEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    val uiState: StateFlow<NewPurchaseUiState> = combine(
        _items,
        _selectedVendor,
        _notes,
        combine(_searchQuery, _searchResults, _isProcessing) { q, r, p -> Triple(q, r, p) }
    ) { items, vendor, notes, (query, results, processing) ->
        NewPurchaseUiState(
            items = items,
            selectedVendor = vendor,
            total = items.sumOf { it.lineTotal },
            notes = notes,
            searchQuery = query,
            searchResults = results,
            isProcessing = processing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NewPurchaseUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            productRepository.searchProducts(query.trim()).collect { products ->
                _searchResults.value = products
            }
        }
    }

    fun addProduct(product: Product) {
        _items.update { items ->
            val existing = items.indexOfFirst { it.product.id == product.id }
            if (existing >= 0) {
                items.toMutableList().apply {
                    val item = this[existing]
                    this[existing] = item.copy(quantity = item.quantity + 1.0)
                }
            } else {
                items + PurchaseCartItem(
                    product = product,
                    costPrice = product.purchasePrice
                )
            }
        }
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    fun removeProduct(productId: Long) {
        _items.update { items -> items.filter { it.product.id != productId } }
    }

    fun updateQuantity(productId: Long, quantity: Double) {
        if (quantity <= 0) {
            removeProduct(productId)
            return
        }
        _items.update { items ->
            items.map { item ->
                if (item.product.id == productId) item.copy(quantity = quantity) else item
            }
        }
    }

    fun updateCostPrice(productId: Long, costPrice: Double) {
        _items.update { items ->
            items.map { item ->
                if (item.product.id == productId) item.copy(costPrice = costPrice.coerceAtLeast(0.0)) else item
            }
        }
    }

    fun selectVendor(vendor: Vendor?) {
        _selectedVendor.value = vendor
    }

    fun onNotesChanged(notes: String) {
        _notes.value = notes
    }

    fun completePurchase() {
        val items = _items.value
        val vendor = _selectedVendor.value

        if (items.isEmpty()) {
            viewModelScope.launch { _event.send(NewPurchaseEvent.ShowError("No items added")) }
            return
        }
        if (vendor == null) {
            viewModelScope.launch { _event.send(NewPurchaseEvent.ShowError("Please select a vendor")) }
            return
        }

        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val total = items.sumOf { it.lineTotal }

                val purchaseItems = items.map { cartItem ->
                    PurchaseItem(
                        productId = cartItem.product.id,
                        productName = cartItem.product.name,
                        quantity = cartItem.quantity,
                        costPrice = cartItem.costPrice,
                        totalPrice = cartItem.lineTotal
                    )
                }

                val purchase = Purchase(
                    vendorId = vendor.id,
                    vendorName = vendor.name,
                    items = purchaseItems,
                    totalAmount = total,
                    notes = _notes.value.trim().ifBlank { null }
                )

                val purchaseId = purchaseRepository.createPurchase(purchase)

                // Add stock for each item and create adjustment entries
                for (cartItem in items) {
                    val product = productRepository.getProductById(cartItem.product.id)
                        ?: continue

                    val newStock = product.currentStock + cartItem.quantity
                    productRepository.updateStock(cartItem.product.id, newStock)

                    stockAdjustmentRepository.addAdjustment(
                        StockAdjustment(
                            productId = cartItem.product.id,
                            productName = cartItem.product.name,
                            previousStock = product.currentStock,
                            newStock = newStock,
                            adjustmentQuantity = cartItem.quantity,
                            reason = "Purchase from ${vendor.name}",
                            type = AdjustmentType.PURCHASE
                        )
                    )

                    // Update product purchase price if it changed
                    if (cartItem.costPrice != product.purchasePrice) {
                        productRepository.updateProduct(
                            product.copy(
                                purchasePrice = cartItem.costPrice,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                }

                // Update vendor payable balance
                val currentPayable = vendor.currentPayableBalance
                vendorRepository.updatePayableBalance(vendor.id, currentPayable + total)

                // Reset state
                _items.value = emptyList()
                _selectedVendor.value = null
                _notes.value = ""

                _isProcessing.value = false
                _event.send(NewPurchaseEvent.PurchaseCompleted(purchaseId))
            } catch (e: Exception) {
                _isProcessing.value = false
                _event.send(
                    NewPurchaseEvent.ShowError(e.message ?: "Failed to complete purchase")
                )
            }
        }
    }
}
