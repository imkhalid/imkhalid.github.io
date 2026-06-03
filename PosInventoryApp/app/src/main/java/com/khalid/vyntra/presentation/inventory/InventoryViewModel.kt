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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Sort modes shown in the Inventory sort sheet. Display label is shown in
 * the filter-bar's "Sort · …" chip and as the row label in the sheet.
 */
enum class InventorySort(val label: String) {
    NAME_ASC("Name A→Z"),
    NAME_DESC("Name Z→A"),
    STOCK_LOW_FIRST("Stock low → high"),
    STOCK_HIGH_FIRST("Stock high → low"),
    PRICE_LOW_FIRST("Price low → high"),
    PRICE_HIGH_FIRST("Price high → low")
}

data class InventoryUiState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryId: Long? = null,
    val sort: InventorySort = InventorySort.NAME_ASC,
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
    private val _sort = MutableStateFlow(InventorySort.NAME_ASC)

    private val _event = Channel<InventoryEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    /**
     * Query+category selection → repository Flow. flatMapLatest so a new
     * filter cancels the previous in-flight query stream.
     */
    private val filteredProducts = combine(
        _searchQuery,
        _selectedCategoryId
    ) { query, categoryId -> query to categoryId }
        .flatMapLatest { (query, categoryId) ->
            when {
                query.isNotBlank() -> productRepository.searchProducts(query.trim())
                categoryId != null -> productRepository.getProductsByCategory(categoryId)
                else -> productRepository.getAllProducts()
            }
        }

    /** Apply the current sort on top of the filtered list. */
    private val sortedProducts = combine(filteredProducts, _sort) { products, sort ->
        when (sort) {
            InventorySort.NAME_ASC -> products.sortedBy { it.name.lowercase() }
            InventorySort.NAME_DESC -> products.sortedByDescending { it.name.lowercase() }
            InventorySort.STOCK_LOW_FIRST -> products.sortedBy { it.currentStock }
            InventorySort.STOCK_HIGH_FIRST -> products.sortedByDescending { it.currentStock }
            InventorySort.PRICE_LOW_FIRST -> products.sortedBy { it.sellingPrice }
            InventorySort.PRICE_HIGH_FIRST -> products.sortedByDescending { it.sellingPrice }
        }
    }

    private val lowStockProducts = productRepository.getLowStockProducts()

    val uiState: StateFlow<InventoryUiState> = combine(
        sortedProducts,
        categoryRepository.getAll(),
        _searchQuery,
        _selectedCategoryId,
        _sort
    ) { products, categories, query, categoryId, sort ->
        InventoryUiState(
            products = products,
            categories = categories,
            searchQuery = query,
            selectedCategoryId = categoryId,
            sort = sort,
            isLoading = false,
            // lowStockCount comes from a separate Flow; injected via the
            // chained combine below to keep this combine ≤ 5 args (typed
            // overload).
            lowStockCount = 0
        )
    }.combine(lowStockProducts.map { it.size }) { state, lowStockCount ->
        state.copy(lowStockCount = lowStockCount)
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

    fun onSortSelected(sort: InventorySort) {
        _sort.value = sort
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
