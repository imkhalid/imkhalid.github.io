package com.khalid.vyntra.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.khalid.vyntra.presentation.components.ConfirmDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataScreen(
    onNavigateBack: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateToImport: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var showFirstConfirm by remember { mutableStateOf(false) }
    var showSecondConfirm by remember { mutableStateOf(false) }

    if (showFirstConfirm) {
        ConfirmDialog(
            title = "Clear all data",
            message = "This will reset all settings to defaults. Continue?",
            confirmLabel = "Continue",
            dismissLabel = "Cancel",
            onConfirm = {
                showFirstConfirm = false
                showSecondConfirm = true
            },
            onDismiss = { showFirstConfirm = false },
            icon = Icons.Filled.Warning,
            isDestructive = true
        )
    }
    if (showSecondConfirm) {
        ConfirmDialog(
            title = "Are you absolutely sure?",
            message = "This action cannot be undone.",
            confirmLabel = "Yes, clear data",
            dismissLabel = "Go back",
            onConfirm = {
                showSecondConfirm = false
                viewModel.confirmClearData()
            },
            onDismiss = { showSecondConfirm = false },
            icon = Icons.Filled.Delete,
            isDestructive = true
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data") },
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
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            SettingsGroupHeader("Transfer")
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Filled.Upload,
                    title = "Export",
                    onClick = onNavigateToExport
                )
                SettingsRow(
                    icon = Icons.Filled.Download,
                    title = "Import",
                    onClick = onNavigateToImport
                )
            }

            SettingsGroupHeader("Danger zone")
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Filled.Delete,
                    title = "Clear all data",
                    onClick = { showFirstConfirm = true },
                    tint = MaterialTheme.colorScheme.error,
                    iconTint = MaterialTheme.colorScheme.error,
                    iconBackground = MaterialTheme.colorScheme.errorContainer
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
