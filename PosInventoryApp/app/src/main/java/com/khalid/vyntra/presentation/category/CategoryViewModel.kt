package com.khalid.vyntra.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalid.vyntra.domain.model.Category
import com.khalid.vyntra.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val dialogState: CategoryDialogState = CategoryDialogState.Hidden
)

sealed interface CategoryDialogState {
    data object Hidden : CategoryDialogState
    data class Shown(
        val editingCategory: Category? = null,
        val name: String = "",
        val description: String = "",
        val nameError: String? = null
    ) : CategoryDialogState
}

sealed interface CategoryEvent {
    data class ShowError(val message: String) : CategoryEvent
    data class CategorySaved(val name: String) : CategoryEvent
    data class CategoryDeleted(val name: String) : CategoryEvent
}

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _dialogState = MutableStateFlow<CategoryDialogState>(CategoryDialogState.Hidden)

    private val _event = Channel<CategoryEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    val uiState: StateFlow<CategoryUiState> = combine(
        categoryRepository.getAll(),
        _dialogState
    ) { categories, dialog ->
        CategoryUiState(
            categories = categories,
            isLoading = false,
            dialogState = dialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoryUiState()
    )

    fun showAddDialog() {
        _dialogState.value = CategoryDialogState.Shown()
    }

    fun showEditDialog(category: Category) {
        _dialogState.value = CategoryDialogState.Shown(
            editingCategory = category,
            name = category.name,
            description = category.description ?: ""
        )
    }

    fun dismissDialog() {
        _dialogState.value = CategoryDialogState.Hidden
    }

    fun onDialogNameChanged(name: String) {
        val current = _dialogState.value
        if (current is CategoryDialogState.Shown) {
            _dialogState.value = current.copy(name = name, nameError = null)
        }
    }

    fun onDialogDescriptionChanged(description: String) {
        val current = _dialogState.value
        if (current is CategoryDialogState.Shown) {
            _dialogState.value = current.copy(description = description)
        }
    }

    fun saveCategory() {
        val dialog = _dialogState.value
        if (dialog !is CategoryDialogState.Shown) return

        if (dialog.name.isBlank()) {
            _dialogState.value = dialog.copy(nameError = "Category name is required")
            return
        }

        viewModelScope.launch {
            try {
                val category = Category(
                    id = dialog.editingCategory?.id ?: 0,
                    name = dialog.name.trim(),
                    description = dialog.description.trim().ifBlank { null }
                )

                if (dialog.editingCategory != null) {
                    categoryRepository.update(category)
                } else {
                    categoryRepository.add(category)
                }

                _dialogState.value = CategoryDialogState.Hidden
                _event.send(CategoryEvent.CategorySaved(category.name))
            } catch (e: Exception) {
                _event.send(CategoryEvent.ShowError(e.message ?: "Failed to save category"))
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            try {
                categoryRepository.delete(category.id)
                _event.send(CategoryEvent.CategoryDeleted(category.name))
            } catch (e: Exception) {
                _event.send(CategoryEvent.ShowError(e.message ?: "Failed to delete category"))
            }
        }
    }
}
