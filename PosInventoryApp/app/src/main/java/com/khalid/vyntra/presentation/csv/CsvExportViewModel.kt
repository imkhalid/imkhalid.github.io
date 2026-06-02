package com.khalid.vyntra.presentation.csv

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalid.vyntra.domain.repository.InvoiceRepository
import com.khalid.vyntra.domain.repository.ProductRepository
import com.khalid.vyntra.util.CsvManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class ExportEntity { PRODUCTS, INVOICES }

data class CsvExportUiState(
    val isExporting: Boolean = false,
    val activeEntity: ExportEntity? = null
)

sealed interface CsvExportEvent {
    data class Success(val entity: ExportEntity, val count: Int) : CsvExportEvent
    data class Failure(val message: String) : CsvExportEvent
}

@HiltViewModel
class CsvExportViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val productRepository: ProductRepository,
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CsvExportUiState())
    val uiState: StateFlow<CsvExportUiState> = _uiState.asStateFlow()

    private val _event = Channel<CsvExportEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun export(entity: ExportEntity, target: Uri) {
        if (_uiState.value.isExporting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, activeEntity = entity) }
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    val resolver = appContext.contentResolver
                    val outputStream = resolver.openOutputStream(target, "wt")
                        ?: error("Cannot open file for writing")

                    outputStream.use { stream ->
                        when (entity) {
                            ExportEntity.PRODUCTS -> {
                                val products = productRepository.getAllProducts().first()
                                CsvManager.exportProducts(products, stream)
                                products.size
                            }
                            ExportEntity.INVOICES -> {
                                val invoices = invoiceRepository.getAll().first()
                                CsvManager.exportInvoices(invoices, stream)
                                invoices.size
                            }
                        }
                    }
                }
            }
            _uiState.update { it.copy(isExporting = false, activeEntity = null) }
            result.fold(
                onSuccess = { count -> _event.send(CsvExportEvent.Success(entity, count)) },
                onFailure = { e -> _event.send(CsvExportEvent.Failure(e.message ?: "Export failed")) }
            )
        }
    }
}
