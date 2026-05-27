package com.khalid.cashlytics.presentation.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalid.cashlytics.data.preferences.PreferencesManager
import com.khalid.cashlytics.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class FreemiumUiState(
    val inventoryWriteCount: Int = 0,
    val reportViewCount: Int = 0,
    val isProUser: Boolean = false,
    val canPerformInventoryWrite: Boolean = true,
    val canViewReport: Boolean = true
)

@HiltViewModel
class FreemiumViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FreemiumUiState())
    val uiState: StateFlow<FreemiumUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                preferencesManager.isProUser,
                preferencesManager.inventoryWriteCount,
                preferencesManager.reportViewCount,
                preferencesManager.lastResetDate
            ) { isPro, writeCount, viewCount, lastResetMillis ->
                val today = LocalDate.now()
                val lastResetDate = if (lastResetMillis > 0) {
                    Instant.ofEpochMilli(lastResetMillis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                } else {
                    null
                }

                val needsReset = lastResetDate == null ||
                        lastResetDate.month != today.month ||
                        lastResetDate.year != today.year

                if (needsReset) {
                    preferencesManager.setInventoryWriteCount(0)
                    preferencesManager.setReportViewCount(0)
                    preferencesManager.setLastResetDate(System.currentTimeMillis())
                    FreemiumUiState(
                        inventoryWriteCount = 0,
                        reportViewCount = 0,
                        isProUser = isPro,
                        canPerformInventoryWrite = true,
                        canViewReport = true
                    )
                } else {
                    FreemiumUiState(
                        inventoryWriteCount = writeCount,
                        reportViewCount = viewCount,
                        isProUser = isPro,
                        canPerformInventoryWrite = isPro || writeCount < Constants.MAX_INVENTORY_WRITES_FREE,
                        canViewReport = isPro || viewCount < Constants.MAX_REPORT_VIEWS_FREE
                    )
                }
            }.collect { _uiState.value = it }
        }
    }

    fun incrementInventoryWriteCount() {
        viewModelScope.launch {
            val current = _uiState.value.inventoryWriteCount
            preferencesManager.setInventoryWriteCount(current + 1)
        }
    }

    fun incrementReportViewCount() {
        viewModelScope.launch {
            val current = _uiState.value.reportViewCount
            preferencesManager.setReportViewCount(current + 1)
        }
    }
}
