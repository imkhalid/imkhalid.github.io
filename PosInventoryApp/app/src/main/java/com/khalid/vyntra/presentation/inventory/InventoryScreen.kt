package com.khalid.vyntra.presentation.inventory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.Check
import com.khalid.vyntra.domain.model.Category
import com.khalid.vyntra.domain.model.Product
import com.khalid.vyntra.presentation.components.ConfirmDialog
import com.khalid.vyntra.presentation.components.EmptyStateView
import com.khalid.vyntra.presentation.components.LoadingIndicator
import com.khalid.vyntra.presentation.components.StockBadge
import com.khalid.vyntra.presentation.components.resolveStockStatus
import com.khalid.vyntra.presentation.navigation.Screen
import com.khalid.vyntra.presentation.theme.SearchBarShape
import com.khalid.vyntra.util.toCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    navController: NavController,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    // Bottom-sheet visibility flags for the filter bar.
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }

    if (showFilterSheet) {
        CategoryFilterSheet(
            categories = uiState.categories,
            selectedCategoryId = uiState.selectedCategoryId,
            onSelect = { id ->
                viewModel.onCategoryFilterSelected(id)
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
        )
    }
    if (showSortSheet) {
        SortSheet(
            current = uiState.sort,
            onSelect = { sort ->
                viewModel.onSortSelected(sort)
                showSortSheet = false
            },
            onDismiss = { showSortSheet = false }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is InventoryEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is InventoryEvent.ProductDeleted -> {
                    snackbarHostState.showSnackbar("${event.productName} deleted")
                }
            }
        }
    }

    // Delete confirmation dialog
    productToDelete?.let { product ->
        ConfirmDialog(
            title = "Delete Product",
            message = "Are you sure you want to delete \"${product.name}\"? This action cannot be undone.",
            confirmLabel = "Delete",
            dismissLabel = "Cancel",
            onConfirm = {
                viewModel.deleteProduct(product)
                productToDelete = null
            },
            onDismiss = { productToDelete = null },
            icon = Icons.Default.Delete,
            isDestructive = true
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Inventory") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (uiState.lowStockCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = MaterialTheme.colorScheme.error) {
                                    Text("${uiState.lowStockCount}")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Low stock alerts",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddEditProduct.createRoute()) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Product",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search products...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = uiState.searchQuery.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = SearchBarShape,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )

            // Compact filter bar (Filters + Sort) replaces the old category
            // chip strip. Chips are tappable; each opens a ModalBottomSheet.
            val activeFilterCount = if (uiState.selectedCategoryId != null) 1 else 0
            com.khalid.vyntra.presentation.components.VyntraFilterBar(
                activeFilterCount = activeFilterCount,
                onFiltersClick = { showFilterSheet = true },
                sortLabel = uiState.sort.label,
                onSortClick = { showSortSheet = true }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Content
            when {
                uiState.isLoading -> {
                    LoadingIndicator()
                }

                uiState.products.isEmpty() -> {
                    EmptyStateView(
                        icon = if (uiState.searchQuery.isNotEmpty()) Icons.Default.Search else Icons.Default.Inventory2,
                        title = if (uiState.searchQuery.isNotEmpty()) "No products found" else "No Products",
                        message = if (uiState.searchQuery.isNotEmpty())
                            "Try a different search term"
                        else
                            "Add your first product to get started",
                        actionLabel = if (uiState.searchQuery.isEmpty()) "Add Product" else null,
                        onAction = if (uiState.searchQuery.isEmpty()) {
                            { navController.navigate(Screen.AddEditProduct.createRoute()) }
                        } else null
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.products,
                            key = { it.id }
                        ) { product ->
                            ProductListItem(
                                product = product,
                                onClick = {
                                    navController.navigate(
                                        Screen.AddEditProduct.createRoute(product.id)
                                    )
                                },
                                onDelete = { productToDelete = product }
                            )
                        }
                        // Bottom spacing for FAB
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductListItem(
    product: Product,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val stockStatus = resolveStockStatus(product.currentStock, product.minStockThreshold)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product icon
            Surface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Product info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (product.sku.isNotBlank()) {
                    Text(
                        text = "SKU: ${product.sku}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!product.categoryName.isNullOrBlank()) {
                    Text(
                        text = product.categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Price and stock
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = product.sellingPrice.toCurrency(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                StockBadge(status = stockStatus)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${product.currentStock.toInt()} ${product.unit}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Category filter sheet ───────────────────────────────────────────────────

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterSheet(
    categories: List<Category>,
    selectedCategoryId: Long?,
    onSelect: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
            Text(
                text = "Filter by category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
            SheetSelectRow(
                label = "All categories",
                selected = selectedCategoryId == null,
                onClick = { onSelect(null) }
            )
            androidx.compose.material3.HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            categories.forEach { category ->
                SheetSelectRow(
                    label = category.name,
                    selected = category.id == selectedCategoryId,
                    onClick = { onSelect(category.id) }
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Sort sheet ──────────────────────────────────────────────────────────────

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun SortSheet(
    current: InventorySort,
    onSelect: (InventorySort) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
            Text(
                text = "Sort by",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
            InventorySort.values().forEach { sort ->
                SheetSelectRow(
                    label = sort.label,
                    selected = sort == current,
                    onClick = { onSelect(sort) }
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SheetSelectRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    androidx.compose.material3.Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
