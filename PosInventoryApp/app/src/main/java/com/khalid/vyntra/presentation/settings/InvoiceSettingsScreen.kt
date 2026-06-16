package com.khalid.vyntra.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Same raw-text-state pattern we use in the hub fields — keeps the cursor
    // stable while typing decimals/partial numbers.
    var prefixText by remember { mutableStateOf(uiState.invoicePrefix) }
    var startNumberText by remember { mutableStateOf(uiState.invoiceStartNumber.toString()) }
    var taxPercentText by remember {
        mutableStateOf(if (uiState.defaultTaxPercent == 0.0) "" else uiState.defaultTaxPercent.toString())
    }

    LaunchedEffect(uiState.invoicePrefix) {
        if (uiState.invoicePrefix != prefixText) prefixText = uiState.invoicePrefix
    }
    LaunchedEffect(uiState.invoiceStartNumber) {
        val current = startNumberText.toIntOrNull()
        if (current != uiState.invoiceStartNumber) {
            startNumberText = uiState.invoiceStartNumber.toString()
        }
    }
    LaunchedEffect(uiState.defaultTaxPercent) {
        val current = taxPercentText.toDoubleOrNull() ?: 0.0
        if (current != uiState.defaultTaxPercent) {
            taxPercentText = if (uiState.defaultTaxPercent == 0.0) "" else uiState.defaultTaxPercent.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = prefixText,
                    onValueChange = {
                        prefixText = it
                        viewModel.updateInvoicePrefix(it)
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Prefix") },
                    placeholder = { Text("INV-") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = startNumberText,
                    onValueChange = { raw ->
                        val filtered = raw.filter { it.isDigit() }
                        startNumberText = filtered
                        filtered.toIntOrNull()?.let { viewModel.updateStartNumber(it) }
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Start number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            OutlinedTextField(
                value = taxPercentText,
                onValueChange = { raw ->
                    val sanitized = raw
                        .replace(',', '.')
                        .filterIndexed { idx, c ->
                            c.isDigit() || (c == '.' && raw.indexOf('.') == idx)
                        }
                    taxPercentText = sanitized
                    viewModel.updateTaxPercent(sanitized.toDoubleOrNull() ?: 0.0)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Default tax %") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("%") }
            )
        }
    }
}
