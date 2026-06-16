package com.khalid.vyntra.presentation.csv

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalid.vyntra.domain.repository.ProductRepository
import com.khalid.vyntra.util.CsvManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class ImportEntity { PRODUCTS }

data class CsvImportUiState(
    val isImporting: Boolean = false,
    val activeEntity: ImportEntity? = null
)

sealed interface CsvImportEvent {
    data class Success(val entity: ImportEntity, val imported: Int, val skipped: Int) : CsvImportEvent
    data class Failure(val message: String) : CsvImportEvent
}

@HiltViewModel
class CsvImportViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CsvImportUiState())
    val uiState: StateFlow<CsvImportUiState> = _uiState.asStateFlow()

    private val _event = Channel<CsvImportEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun import(entity: ImportEntity, source: Uri) {
        if (_uiState.value.isImporting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, activeEntity = entity) }
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    val resolver = appContext.contentResolver
                    val inputStream = resolver.openInputStream(source)
                        ?: error("Cannot open selected file")

                    inputStream.use { stream ->
                        when (entity) {
                            ImportEntity.PRODUCTS -> {
                                val parsed = CsvManager.importProducts(stream)
                                var inserted = 0
                                for (product in parsed) {
                                    try {
                                        // New rows: id == 0 → insert; otherwise update by id
                                        if (product.id == 0L) {
                                            productRepository.addProduct(product)
                                        } else {
                                            val existing = productRepository.getProductById(product.id)
                                            if (existing == null) {
                                                productRepository.addProduct(product.copy(id = 0))
                                            } else {
                                                productRepository.updateProduct(product)
                                            }
                                        }
                                        inserted++
                                    } catch (_: Exception) { /* row error → skip */ }
                                }
                                inserted to (parsed.size - inserted)
                            }
                        }
                    }
                }
            }
            _uiState.update { it.copy(isImporting = false, activeEntity = null) }
            result.fold(
                onSuccess = { (inserted, skipped) ->
                    _event.send(CsvImportEvent.Success(entity, inserted, skipped))
                },
                onFailure = { e ->
                    _event.send(CsvImportEvent.Failure(e.message ?: "Import failed"))
                }
            )
        }
    }
}
