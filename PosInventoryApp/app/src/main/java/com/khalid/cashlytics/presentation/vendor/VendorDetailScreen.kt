package com.khalid.cashlytics.presentation.vendor

import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khalid.cashlytics.domain.model.PaymentMethod
import com.khalid.cashlytics.domain.model.PaymentRecord
import com.khalid.cashlytics.domain.model.Purchase
import com.khalid.cashlytics.presentation.theme.LossRed
import com.khalid.cashlytics.presentation.theme.ProfitGreen
import com.khalid.cashlytics.util.toFormattedDate
import com.khalid.cashlytics.util.toFormattedDateTime
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorDetailScreen(
    viewModel: VendorDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEditVendor: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "PK")) }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is VendorDetailEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }

                is VendorDetailEvent.PaymentRecorded -> {
                    Toast.makeText(context, "Payment recorded successfully", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    // Payment dialog
    val dialogState = uiState.paymentDialogState
    if (dialogState is VendorPaymentDialogState.Shown) {
        VendorRecordPaymentDialog(
            dialogState = dialogState,
            onAmountChanged = viewModel::onPaymentAmountChanged,
            onMethodChanged = viewModel::onPaymentMethodChanged,
            onNotesChanged = viewModel::onPaymentNotesChanged,
            onConfirm = viewModel::recordPayment,
            onDismiss = viewModel::dismissPaymentDialog
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vendor Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    uiState.vendor?.let { vendor ->
                        IconButton(onClick = { onNavigateToEditVendor(vendor.id) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Vendor"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val vendor = uiState.vendor ?: return@Scaffold

            var selectedTab by rememberSaveable { mutableIntStateOf(0) }
            val tabs = listOf("Purchases", "Payments")

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Vendor info card
                item {
                    VendorInfoCard(
                        name = vendor.name,
                        company = vendor.company,
                        phone = vendor.phone,
                        email = vendor.email,
                        address = vendor.address,
                        bankDetails = vendor.bankDetails,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Payable balance
                item {
                    PayableBalanceSection(
                        balance = uiState.payableBalance,
                        formattedBalance = currencyFormat.format(uiState.payableBalance),
                        onRecordPayment = viewModel::showPaymentDialog,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Tab row
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    TabRow(selectedTabIndex = selectedTab) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title) }
                            )
                        }
                    }
                }

                // Tab content
                when (selectedTab) {
                    0 -> {
                        if (uiState.purchases.isEmpty()) {
                            item {
                                EmptyTabContent(message = "No purchases yet")
                            }
                        } else {
                            items(
                                items = uiState.purchases,
                                key = { it.id }
                            ) { purchase ->
                                PurchaseHistoryItem(
                                    purchase = purchase,
                                    currencyFormat = currencyFormat
                                )
                            }
                        }
                    }

                    1 -> {
                        if (uiState.payments.isEmpty()) {
                            item {
                                EmptyTabContent(message = "No payments recorded")
                            }
                        } else {
                            items(
                                items = uiState.payments,
                                key = { it.id }
                            ) { payment ->
                                VendorPaymentHistoryItem(
                                    payment = payment,
                                    currencyFormat = currencyFormat
                                )
                            }
                        }
                    }
                }

                // Bottom spacing
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun VendorInfoCard(
    name: String,
    company: String?,
    phone: String?,
    email: String?,
    address: String?,
    bankDetails: String?,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (!company.isNullOrBlank()) {
                VendorInfoRow(
                    icon = Icons.Default.Business,
                    label = "Company",
                    value = company
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (!phone.isNullOrBlank()) {
                VendorInfoRow(
                    icon = Icons.Default.Phone,
                    label = "Phone",
                    value = phone
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (!email.isNullOrBlank()) {
                VendorInfoRow(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = email
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (!address.isNullOrBlank()) {
                VendorInfoRow(
                    icon = Icons.Default.Home,
                    label = "Address",
                    value = address
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (!bankDetails.isNullOrBlank()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                VendorInfoRow(
                    icon = Icons.Default.AccountBalance,
                    label = "Bank Details",
                    value = bankDetails
                )
            }
        }
    }
}

@Composable
private fun VendorInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PayableBalanceSection(
    balance: Double,
    formattedBalance: String,
    onRecordPayment: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (balance > 0)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Payable Balance",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formattedBalance,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (balance > 0) LossRed else ProfitGreen
            )
            if (balance > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onRecordPayment) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Record Payment")
                }
            }
        }
    }
}

@Composable
private fun VendorRecordPaymentDialog(
    dialogState: VendorPaymentDialogState.Shown,
    onAmountChanged: (String) -> Unit,
    onMethodChanged: (PaymentMethod) -> Unit,
    onNotesChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Payment") },
        text = {
            Column {
                OutlinedTextField(
                    value = dialogState.amount,
                    onValueChange = onAmountChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Amount") },
                    placeholder = { Text("0.00") },
                    isError = dialogState.amountError != null,
                    supportingText = dialogState.amountError?.let { error ->
                        { Text(text = error, color = MaterialTheme.colorScheme.error) }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Payment Method",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentMethod.entries.forEach { method ->
                        FilterChip(
                            selected = dialogState.paymentMethod == method,
                            onClick = { onMethodChanged(method) },
                            label = { Text(method.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = dialogState.notes,
                    onValueChange = onNotesChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Notes") },
                    placeholder = { Text("Optional notes") },
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PurchaseHistoryItem(
    purchase: Purchase,
    currencyFormat: NumberFormat
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${purchase.items.size} item${if (purchase.items.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = purchase.createdAt.toFormattedDate(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!purchase.notes.isNullOrBlank()) {
                    Text(
                        text = purchase.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = currencyFormat.format(purchase.totalAmount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun VendorPaymentHistoryItem(
    payment: PaymentRecord,
    currencyFormat: NumberFormat
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Payment,
                contentDescription = null,
                tint = ProfitGreen,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = payment.paymentMethod.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = payment.createdAt.toFormattedDateTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!payment.notes.isNullOrBlank()) {
                    Text(
                        text = payment.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = currencyFormat.format(payment.amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ProfitGreen
            )
        }
    }
}

@Composable
private fun EmptyTabContent(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
