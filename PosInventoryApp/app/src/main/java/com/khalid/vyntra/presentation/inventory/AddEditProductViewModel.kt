package com.khalid.vyntra.presentation.inventory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalid.vyntra.domain.model.Category
import com.khalid.vyntra.domain.model.Product
import com.khalid.vyntra.domain.model.Vendor
import com.khalid.vyntra.domain.repository.CategoryRepository
import com.khalid.vyntra.domain.repository.ProductRepository
import com.khalid.vyntra.domain.repository.VendorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditProductUiState(
    val isEditMode: Boolean = false,
    val productId: Long = 0,
    val name: String = "",
    val sku: String = "",
    val barcode: String = "",
    val categoryId: Long? = null,
    val unit: String = "pcs",
    val purchasePrice: String = "",
    val sellingPrice: String = "",
    val currentStock: String = "",
    val minStockThreshold: String = "",
    val supplierId: Long? = null,
    val imagePath: String? = null,
    val notes: String = "",
    val categories: List<Category> = emptyList(),
    val vendors: List<Vendor> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val validationErrors: Map<String, String> = emptyMap()
)

sealed interface AddEditProductEvent {
    data object SaveSuccess : AddEditProductEvent
    data class ShowError(val message: String) : AddEditProductEvent
}

@HiltViewModel
class AddEditProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val vendorRepository: VendorRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId: Long = savedStateHandle.get<Long>("productId") ?: 0L

    private val _uiState = MutableStateFlow(
        AddEditProductUiState(isEditMode = productId > 0, productId = productId)
    )
    val uiState: StateFlow<AddEditProductUiState> = _uiState.asStateFlow()

    private val _event = Channel<AddEditProductEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        loadDropdownData()
        if (productId > 0) {
            loadProduct(productId)
        }
    }

    private fun loadDropdownData() {
        viewModelScope.launch {
            categoryRepository.getAll().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
        viewModelScope.launch {
            vendorRepository.getAll().collect { vendors ->
                _uiState.update { it.copy(vendors = vendors) }
            }
        }
    }

    private fun loadProduct(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val product = productRepository.getProductById(id)
                if (product != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = product.name,
                            sku = product.sku,
                            barcode = product.barcode ?: "",
                            categoryId = product.categoryId,
                            unit = product.unit,
                            purchasePrice = product.purchasePrice.toBigDecimal().stripTrailingZeros().toPlainString(),
                            sellingPrice = product.sellingPrice.toBigDecimal().stripTrailingZeros().toPlainString(),
                            currentStock = product.currentStock.toBigDecimal().stripTrailingZeros().toPlainString(),
                            minStockThreshold = product.minStockThreshold.toBigDecimal().stripTrailingZeros().toPlainString(),
                            supplierId = product.supplierId,
                            imagePath = product.imagePath,
                            notes = product.notes ?: ""
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    _event.send(AddEditProductEvent.ShowError("Product not found"))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _event.send(AddEditProductEvent.ShowError(e.message ?: "Failed to load product"))
            }
        }
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value, validationErrors = it.validationErrors - "name") }
    }

    fun onSkuChanged(value: String) {
        _uiState.update { it.copy(sku = value) }
    }

    fun onBarcodeChanged(value: String) {
        _uiState.update { it.copy(barcode = value) }
    }

    fun onCategorySelected(categoryId: Long?) {
        _uiState.update { it.copy(categoryId = categoryId) }
    }

    fun onUnitChanged(value: String) {
        _uiState.update { it.copy(unit = value) }
    }

    fun onPurchasePriceChanged(value: String) {
        _uiState.update { it.copy(purchasePrice = value, validationErrors = it.validationErrors - "purchasePrice") }
    }

    fun onSellingPriceChanged(value: String) {
        _uiState.update { it.copy(sellingPrice = value, validationErrors = it.validationErrors - "sellingPrice") }
    }

    fun onCurrentStockChanged(value: String) {
        _uiState.update { it.copy(currentStock = value, validationErrors = it.validationErrors - "currentStock") }
    }

    fun onMinStockThresholdChanged(value: String) {
        _uiState.update { it.copy(minStockThreshold = value) }
    }

    fun onSupplierSelected(vendorId: Long?) {
        _uiState.update { it.copy(supplierId = vendorId) }
    }

    fun onImagePathChanged(path: String?) {
        _uiState.update { it.copy(imagePath = path) }
    }

    fun onNotesChanged(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    /**
     * Quick-add a new category from inside the product form and auto-select
     * it. Triggered by the "+ Add new category" row at the top of the
     * Category dropdown.
     */
    fun createCategoryQuick(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            try {
                val newId = categoryRepository.add(Category(name = trimmed))
                // Optimistic select; the categories Flow refreshes the list.
                _uiState.update { it.copy(categoryId = newId) }
            } catch (e: Exception) {
                _event.send(AddEditProductEvent.ShowError(e.message ?: "Failed to add category"))
            }
        }
    }

    /**
     * Quick-add a new vendor from inside the product form and auto-select it.
     * Triggered by the "+ Add new vendor" row at the top of the Vendor dropdown.
     */
    fun createVendorQuick(name: String, phone: String?) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            try {
                val newId = vendorRepository.add(
                    Vendor(
                        name = trimmed,
                        company = null,
                        phone = phone?.trim()?.ifBlank { null }
                    )
                )
                _uiState.update { it.copy(supplierId = newId) }
            } catch (e: Exception) {
                _event.send(AddEditProductEvent.ShowError(e.message ?: "Failed to add vendor"))
            }
        }
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
                val product = Product(
                    id = if (state.isEditMode) state.productId else 0,
                    name = state.name.trim(),
                    sku = state.sku.trim(),
                    barcode = state.barcode.trim().ifBlank { null },
                    categoryId = state.categoryId,
                    unit = state.unit.trim(),
                    purchasePrice = state.purchasePrice.toDoubleOrNull() ?: 0.0,
                    sellingPrice = state.sellingPrice.toDoubleOrNull() ?: 0.0,
                    currentStock = state.currentStock.toDoubleOrNull() ?: 0.0,
                    minStockThreshold = state.minStockThreshold.toDoubleOrNull() ?: 0.0,
                    supplierId = state.supplierId,
                    imagePath = state.imagePath,
                    notes = state.notes.trim().ifBlank { null }
                )

                if (state.isEditMode) {
                    productRepository.updateProduct(product.copy(updatedAt = System.currentTimeMillis()))
                } else {
                    productRepository.addProduct(product)
                }

                _uiState.update { it.copy(isSaving = false) }
                _event.send(AddEditProductEvent.SaveSuccess)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                _event.send(AddEditProductEvent.ShowError(e.message ?: "Failed to save product"))
            }
        }
    }

    private fun validate(state: AddEditProductUiState): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (state.name.isBlank()) {
            errors["name"] = "Product name is required"
        }
        val purchasePrice = state.purchasePrice.toDoubleOrNull()
        if (purchasePrice == null || purchasePrice < 0) {
            errors["purchasePrice"] = "Valid purchase price is required"
        }
        val sellingPrice = state.sellingPrice.toDoubleOrNull()
        if (sellingPrice == null || sellingPrice < 0) {
            errors["sellingPrice"] = "Valid selling price is required"
        }
        val currentStock = state.currentStock.toDoubleOrNull()
        if (currentStock == null || currentStock < 0) {
            errors["currentStock"] = "Stock must be 0 or greater"
        }
        return errors
    }
}
