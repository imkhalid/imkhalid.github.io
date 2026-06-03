package com.khalid.vyntra.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.khalid.vyntra.presentation.navigation.Screen
import com.khalid.vyntra.presentation.theme.ProfitGreen

/**
 * Settings hub. Each top-level row drills into a dedicated sub-screen
 * (Business profile, Invoice, Display, Data, About). Pro/upgrade card
 * lives inline so the user always sees it.
 *
 * The shared SettingsViewModel feeds trailing-summary values (current
 * invoice prefix + start number, display mode, etc.) so the user can
 * verify settings without opening every screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            item {
                BusinessIdentityCard(
                    name = uiState.businessName.ifBlank { "Your business" },
                    onClick = { navController.navigate(Screen.SettingsBusinessProfile.route) }
                )
            }

            item { SettingsGroupHeader("Sales") }
            item {
                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Filled.Receipt,
                        title = "Invoice",
                        trailingValue = invoiceSummary(uiState),
                        onClick = { navController.navigate(Screen.SettingsInvoice.route) }
                    )
                    SettingsRow(
                        icon = Icons.Filled.DarkMode,
                        title = "Display",
                        trailingValue = displaySummary(uiState),
                        onClick = { navController.navigate(Screen.SettingsDisplay.route) }
                    )
                }
            }

            item { SettingsGroupHeader("Data") }
            item {
                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Filled.Storage,
                        title = "Export, import & clear",
                        onClick = { navController.navigate(Screen.SettingsData.route) }
                    )
                }
            }

            item { SettingsGroupHeader("Plan") }
            item {
                ProCard(
                    isProUser = uiState.isProUser,
                    onUpgrade = { navController.navigate(Screen.Premium.route) }
                )
            }

            item { SettingsGroupHeader("App") }
            item {
                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Filled.Info,
                        title = "About",
                        trailingValue = "v1.0.0",
                        onClick = { navController.navigate(Screen.SettingsAbout.route) }
                    )
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ── Business identity card ──────────────────────────────────────────────────

@Composable
private fun BusinessIdentityCard(name: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = initials(name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Business profile",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Edit",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ── Pro upgrade card ────────────────────────────────────────────────────────

@Composable
private fun ProCard(isProUser: Boolean, onUpgrade: () -> Unit = {}) {
    if (isProUser) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = ProfitGreen.copy(alpha = 0.12f))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = null,
                    tint = ProfitGreen,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Pro unlocked", fontWeight = FontWeight.Bold, color = ProfitGreen)
                    Text(
                        text = "All features available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    } else {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Upgrade to Pro",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "Unlimited products, reports, no ads",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(onClick = onUpgrade, modifier = Modifier.fillMaxWidth()) {
                    Text("See plans & upgrade")
                }
            }
        }
    }
}

// ── Helpers ─────────────────────────────────────────────────────────────────

private fun invoiceSummary(state: SettingsUiState): String {
    val prefix = state.invoicePrefix.ifBlank { "INV-" }
    return "$prefix${state.invoiceStartNumber}"
}

private fun displaySummary(state: SettingsUiState): String {
    val currency = state.currencySymbol
    return if (state.isDarkTheme) "$currency · Dark" else currency
}

private fun initials(name: String): String {
    val parts = name.trim().split(' ').filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts.first().take(2).uppercase()
        else -> (parts.first().first().toString() + parts.last().first().toString()).uppercase()
    }
}
