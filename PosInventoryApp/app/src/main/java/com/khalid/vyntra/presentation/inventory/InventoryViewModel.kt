package com.khalid.vyntra.presentation.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalid.vyntra.domain.model.Category
import com.khalid.vyntra.domain.model.Product
import com.khalid.vyntra.domain.repository.CategoryRepository
import com.khalid.vyntra.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventoryUiState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryId: Long? = null,
    val isLoading: Boolean = true,
    val lowStockCount: Int = 0
)

sealed interface InventoryEvent {
    data class ShowError(val message: String) : InventoryEvent
    data class ProductDeleted(val productName: String) : InventoryEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)

    private val _event = Channel<InventoryEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    private val filteredProducts = combine(
        _searchQuery,
        _selectedCategoryId
    ) { query, categoryId ->
        Pair(query, categoryId)
    }.flatMapLatest { (query, categoryId) ->
        when {
            query.isNotBlank() -> productRepository.searchProducts(query.trim())
            categoryId != null -> productRepository.getProductsByCategory(categoryId)
            else -> productRepository.getAllProducts()
        }
    }

    private val lowStockProducts = productRepository.getLowStockProducts()

    val uiState: StateFlow<InventoryUiState> = combine(
        filteredProducts,
        categoryRepository.getAll(),
        _searchQuery,
        _selectedCategoryId,
        lowStockProducts.map { it.size }
    ) { products, categories, query, categoryId, lowStockCount ->
        InventoryUiState(
            products = products,
            categories = categories,
            searchQuery = query,
            selectedCategoryId = categoryId,
            isLoading = false,
            lowStockCount = lowStockCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InventoryUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategoryFilterSelected(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            try {
                productRepository.deleteProduct(product.id)
                _event.send(InventoryEvent.ProductDeleted(product.name))
            } catch (e: Exception) {
                _event.send(InventoryEvent.ShowError(e.message ?: "Failed to delete product"))
            }
        }
    }
}
