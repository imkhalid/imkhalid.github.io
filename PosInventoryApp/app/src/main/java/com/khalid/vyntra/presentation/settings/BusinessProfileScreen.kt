package com.khalid.vyntra.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Hold the in-flight text locally so the cursor doesn't jump while the
    // VM's DataStore-backed flow re-emits asynchronously. Sync FROM the VM
    // only when the persisted value diverges from what the user has typed
    // (e.g. settings reset, restored from backup).
    var nameText by remember { mutableStateOf(uiState.businessName) }
    var addressText by remember { mutableStateOf(uiState.businessAddress) }
    var taxText by remember { mutableStateOf(uiState.taxNumber) }

    LaunchedEffect(uiState.businessName) {
        if (uiState.businessName != nameText) nameText = uiState.businessName
    }
    LaunchedEffect(uiState.businessAddress) {
        if (uiState.businessAddress != addressText) addressText = uiState.businessAddress
    }
    LaunchedEffect(uiState.taxNumber) {
        if (uiState.taxNumber != taxText) taxText = uiState.taxNumber
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Business Profile") },
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
            OutlinedTextField(
                value = nameText,
                onValueChange = {
                    nameText = it
                    viewModel.updateBusinessName(it)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Business name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            OutlinedTextField(
                value = addressText,
                onValueChange = {
                    addressText = it
                    viewModel.updateBusinessAddress(it)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Address") },
                minLines = 2,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
            OutlinedTextField(
                value = taxText,
                onValueChange = {
                    taxText = it
                    viewModel.updateTaxNumber(it)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tax number / NTN") },
                singleLine = true
            )
        }
    }
}
