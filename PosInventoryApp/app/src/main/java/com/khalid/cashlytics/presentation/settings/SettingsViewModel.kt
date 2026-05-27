package com.khalid.cashlytics.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalid.cashlytics.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val businessName: String = "",
    val businessAddress: String = "",
    val taxNumber: String = "",
    val invoicePrefix: String = "INV-",
    val invoiceStartNumber: Int = 1,
    val defaultTaxPercent: Double = 0.0,
    val currencySymbol: String = "Rs",
    val isDarkTheme: Boolean = false,
    val isProUser: Boolean = false,
    val showClearDataDialog: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _showClearDataDialog = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            preferencesManager.businessName,
            preferencesManager.businessAddress,
            preferencesManager.taxNumber,
            preferencesManager.invoicePrefix,
            preferencesManager.invoiceStartNumber
        ) { name, address, taxNumber, prefix, startNumber ->
            SettingsPartial1(name, address, taxNumber, prefix, startNumber)
        },
        combine(
            preferencesManager.defaultTaxPercent,
            preferencesManager.currencySymbol,
            preferencesManager.isDarkTheme,
            preferencesManager.isProUser,
            _showClearDataDialog
        ) { taxPercent, currency, darkTheme, proUser, showDialog ->
            SettingsPartial2(taxPercent, currency, darkTheme, proUser, showDialog)
        }
    ) { part1, part2 ->
        SettingsUiState(
            businessName = part1.businessName,
            businessAddress = part1.businessAddress,
            taxNumber = part1.taxNumber,
            invoicePrefix = part1.invoicePrefix,
            invoiceStartNumber = part1.invoiceStartNumber,
            defaultTaxPercent = part2.defaultTaxPercent,
            currencySymbol = part2.currencySymbol,
            isDarkTheme = part2.isDarkTheme,
            isProUser = part2.isProUser,
            showClearDataDialog = part2.showClearDataDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun updateBusinessName(name: String) {
        viewModelScope.launch { preferencesManager.setBusinessName(name) }
    }

    fun updateBusinessAddress(address: String) {
        viewModelScope.launch { preferencesManager.setBusinessAddress(address) }
    }

    fun updateTaxNumber(taxNumber: String) {
        viewModelScope.launch { preferencesManager.setTaxNumber(taxNumber) }
    }

    fun updateInvoicePrefix(prefix: String) {
        viewModelScope.launch { preferencesManager.setInvoicePrefix(prefix) }
    }

    fun updateStartNumber(number: Int) {
        viewModelScope.launch { preferencesManager.setInvoiceStartNumber(number) }
    }

    fun updateTaxPercent(percent: Double) {
        viewModelScope.launch { preferencesManager.setDefaultTaxPercent(percent) }
    }

    fun updateCurrencySymbol(symbol: String) {
        viewModelScope.launch { preferencesManager.setCurrencySymbol(symbol) }
    }

    fun toggleDarkTheme() {
        viewModelScope.launch {
            val current = uiState.value.isDarkTheme
            preferencesManager.setDarkTheme(!current)
        }
    }

    fun showClearDialog() {
        _showClearDataDialog.value = true
    }

    fun dismissClearDialog() {
        _showClearDataDialog.value = false
    }

    fun confirmClearData() {
        viewModelScope.launch {
            preferencesManager.setBusinessName("")
            preferencesManager.setBusinessAddress("")
            preferencesManager.setTaxNumber("")
            preferencesManager.setInvoicePrefix("INV-")
            preferencesManager.setInvoiceStartNumber(1)
            preferencesManager.setDefaultTaxPercent(0.0)
            preferencesManager.setCurrencySymbol("Rs")
            _showClearDataDialog.value = false
        }
    }
}

/**
 * Internal holder to work around [combine]'s 5-parameter limit.
 */
private data class SettingsPartial1(
    val businessName: String,
    val businessAddress: String,
    val taxNumber: String,
    val invoicePrefix: String,
    val invoiceStartNumber: Int
)

/**
 * Internal holder to work around [combine]'s 5-parameter limit.
 */
private data class SettingsPartial2(
    val defaultTaxPercent: Double,
    val currencySymbol: String,
    val isDarkTheme: Boolean,
    val isProUser: Boolean,
    val showClearDataDialog: Boolean
)
