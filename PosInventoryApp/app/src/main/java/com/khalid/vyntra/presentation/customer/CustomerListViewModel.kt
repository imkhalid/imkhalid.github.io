package com.khalid.vyntra.presentation.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalid.vyntra.domain.model.Customer
import com.khalid.vyntra.domain.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerListUiState(
    val customers: List<Customer> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

sealed interface CustomerListEvent {
    data class ShowError(val message: String) : CustomerListEvent
    data class CustomerDeleted(val name: String) : CustomerListEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CustomerListViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    private val _event = Channel<CustomerListEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    private val filteredCustomers = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            customerRepository.getAll()
        } else {
            customerRepository.search(query.trim())
        }
    }

    val uiState: StateFlow<CustomerListUiState> = combine(
        filteredCustomers,
        _searchQuery
    ) { customers, query ->
        CustomerListUiState(
            customers = customers,
            searchQuery = query,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CustomerListUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            try {
                customerRepository.delete(customer.id)
                _event.send(CustomerListEvent.CustomerDeleted(customer.name))
            } catch (e: Exception) {
                _event.send(CustomerListEvent.ShowError(e.message ?: "Failed to delete customer"))
            }
        }
    }
}
