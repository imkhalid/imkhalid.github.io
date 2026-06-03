package com.khalid.vyntra.presentation.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.khalid.vyntra.presentation.components.LoadingIndicator
import com.khalid.vyntra.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    navController: NavController,
    viewModel: AddEditProductViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is AddEditProductEvent.SaveSuccess -> {
                    navController.popBackStack()
                }
                is AddEditProductEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) "Edit Product" else "Add Product"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingIndicator(modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Basic Information ────────────────────────────────────
                SectionLabel("Basic Information")

                // Product Name
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Product Name *") },
                    isError = uiState.validationErrors.containsKey("name"),
                    supportingText = uiState.validationErrors["name"]?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                // SKU
                OutlinedTextField(
                    value = uiState.sku,
                    onValueChange = viewModel::onSkuChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("SKU") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                // Barcode with scanner button
                OutlinedTextField(
                    value = uiState.barcode,
                    onValueChange = viewModel::onBarcodeChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Barcode") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(
                            onClick = { navController.navigate(Screen.BarcodeScanner.route) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan barcode"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                // Unit
                OutlinedTextField(
                    value = uiState.unit,
                    onValueChange = viewModel::onUnitChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Unit") },
                    placeholder = { Text("e.g. pcs, kg, litre") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                // ── Category & Vendor ────────────────────────────────────
                SectionLabel("Category & Vendor")

                // Category dropdown
                CategoryDropdown(
                    categories = uiState.categories,
                    selectedCategoryId = uiState.categoryId,
                    onCategorySelected = viewModel::onCategorySelected,
                    onCreateCategory = viewModel::createCategoryQuick
                )

                // Vendor dropdown
                VendorDropdown(
                    vendors = uiState.vendors,
                    selectedVendorId = uiState.supplierId,
                    onVendorSelected = viewModel::onSupplierSelected,
                    onCreateVendor = viewModel::createVendorQuick
                )

                // ── Pricing ──────────────────────────────────────────────
                SectionLabel("Pricing")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Purchase Price
                    OutlinedTextField(
                        value = uiState.purchasePrice,
                        onValueChange = viewModel::onPurchasePriceChanged,
                        modifier = Modifier.weight(1f),
                        label = { Text("Purchase Price *") },
                        prefix = { Text("Rs ") },
                        isError = uiState.validationErrors.containsKey("purchasePrice"),
                        supportingText = uiState.validationErrors["purchasePrice"]?.let {
                            { Text(it, color = MaterialTheme.colorScheme.error) }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        )
                    )

                    // Selling Price
                    OutlinedTextField(
                        value = uiState.sellingPrice,
                        onValueChange = viewModel::onSellingPriceChanged,
                        modifier = Modifier.weight(1f),
                        label = { Text("Selling Price *") },
                        prefix = { Text("Rs ") },
                        isError = uiState.validationErrors.containsKey("sellingPrice"),
                        supportingText = uiState.validationErrors["sellingPrice"]?.let {
                            { Text(it, color = MaterialTheme.colorScheme.error) }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        )
                    )
                }

                // Profit margin display
                val purchasePrice = uiState.purchasePrice.toDoubleOrNull()
                val sellingPrice = uiState.sellingPrice.toDoubleOrNull()
                if (purchasePrice != null && purchasePrice > 0 && sellingPrice != null) {
                    val margin = ((sellingPrice - purchasePrice) / purchasePrice) * 100
                    val marginColor = if (margin >= 0)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                    Text(
                        text = "Profit Margin: ${String.format("%.1f", margin)}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = marginColor,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // ── Stock ────────────────────────────────────────────────
                SectionLabel("Stock")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Current Stock
                    OutlinedTextField(
                        value = uiState.currentStock,
                        onValueChange = viewModel::onCurrentStockChanged,
                        modifier = Modifier.weight(1f),
                        label = { Text("Current Stock *") },
                        isError = uiState.validationErrors.containsKey("currentStock"),
                        supportingText = uiState.validationErrors["currentStock"]?.let {
                            { Text(it, color = MaterialTheme.colorScheme.error) }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        )
                    )

                    // Min Stock Threshold
                    OutlinedTextField(
                        value = uiState.minStockThreshold,
                        onValueChange = viewModel::onMinStockThresholdChanged,
                        modifier = Modifier.weight(1f),
                        label = { Text("Min Threshold") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        )
                    )
                }

                // ── Notes ────────────────────────────────────────────────
                SectionLabel("Additional")

                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::onNotesChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Notes") },
                    minLines = 3,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ── Save Button ──────────────────────────────────────────
                Button(
                    onClick = viewModel::save,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !uiState.isSaving,
                    shape = MaterialTheme.shapes.medium
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .height(20.dp)
                                .width(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(
                        text = if (uiState.isEditMode) "Update Product" else "Save Product",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    categories: List<com.khalid.vyntra.domain.model.Category>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    onCreateCategory: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    val selectedCategoryName = categories.find { it.id == selectedCategoryId }?.name ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedCategoryName,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            readOnly = true,
            label = { Text("Category") },
            placeholder = { Text("Select category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // Pinned "+ Add new category" — primary-tinted so it reads as
            // an action, not a value.
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Add new category",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                onClick = {
                    expanded = false
                    showAddDialog = true
                },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("None") },
                onClick = {
                    onCategorySelected(null)
                    expanded = false
                },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onCategorySelected(category.id)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }

    if (showAddDialog) {
        QuickAddCategoryDialog(
            onConfirm = { name ->
                onCreateCategory(name)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VendorDropdown(
    vendors: List<com.khalid.vyntra.domain.model.Vendor>,
    selectedVendorId: Long?,
    onVendorSelected: (Long?) -> Unit,
    onCreateVendor: (name: String, phone: String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    val selectedVendorName = vendors.find { it.id == selectedVendorId }?.name ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedVendorName,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            readOnly = true,
            label = { Text("Vendor / Supplier") },
            placeholder = { Text("Select vendor") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Add new vendor",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                onClick = {
                    expanded = false
                    showAddDialog = true
                },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("None") },
                onClick = {
                    onVendorSelected(null)
                    expanded = false
                },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
            )
            vendors.forEach { vendor ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(vendor.name)
                            if (!vendor.company.isNullOrBlank()) {
                                Text(
                                    text = vendor.company,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    onClick = {
                        onVendorSelected(vendor.id)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }

    if (showAddDialog) {
        QuickAddVendorDialog(
            onConfirm = { name, phone ->
                onCreateVendor(name, phone)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun QuickAddCategoryDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New category") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Category name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun QuickAddVendorDialog(
    onConfirm: (name: String, phone: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New vendor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Name *") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Phone (optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, phone.ifBlank { null }) },
                enabled = name.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
