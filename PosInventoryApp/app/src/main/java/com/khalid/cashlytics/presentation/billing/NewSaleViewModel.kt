package com.khalid.cashlytics.presentation.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalid.cashlytics.data.preferences.PreferencesManager
import com.khalid.cashlytics.domain.model.AdjustmentType
import com.khalid.cashlytics.domain.model.Customer
import com.khalid.cashlytics.domain.model.Invoice
import com.khalid.cashlytics.domain.model.InvoiceItem
import com.khalid.cashlytics.domain.model.InvoiceStatus
import com.khalid.cashlytics.domain.model.PaymentMethod
import com.khalid.cashlytics.domain.model.Product
import com.khalid.cashlytics.domain.model.StockAdjustment
import com.khalid.cashlytics.domain.repository.CustomerRepository
import com.khalid.cashlytics.domain.repository.InvoiceRepository
import com.khalid.cashlytics.domain.repository.ProductRepository
import com.khalid.cashlytics.domain.repository.StockAdjustmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartItem(
    val product: Product,
    val quantity: Double = 1.0,
    val discount: Double = 0.0
) {
    val unitPrice: Double get() = product.sellingPrice
    val lineTotal: Double get() = (unitPrice * quantity) - discount
}

data class NewSaleUiState(
    val cartItems: List<CartItem> = emptyList(),
    val selectedCustomer: Customer? = null,
    val subtotal: Double = 0.0,
    val overallDiscount: Double = 0.0,
    val taxPercent: Double = 0.0,
    val taxAmount: Double = 0.0,
    val total: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val paidAmount: String = "",
    val changeAmount: Double = 0.0,
    val searchQuery: String = "",
    val searchResults: List<Product> = emptyList(),
    val isProcessing: Boolean = false,
    val currencySymbol: String = "Rs"
)

sealed interface NewSaleEvent {
    data class ShowError(val message: String) : NewSaleEvent
    data class SaleCompleted(val invoiceNumber: String) : NewSaleEvent
}

@HiltViewModel
class NewSaleViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val invoiceRepository: InvoiceRepository,
    private val stockAdjustmentRepository: StockAdjustmentRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    private val _overallDiscount = MutableStateFlow(0.0)
    private val _paymentMethod = MutableStateFlow(PaymentMethod.CASH)
    private val _paidAmount = MutableStateFlow("")
    private val _searchQuery = MutableStateFlow("")
    private val _searchResults = MutableStateFlow<List<Product>>(emptyList())
    private val _isProcessing = MutableStateFlow(false)

    private val _event = Channel<NewSaleEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    val uiState: StateFlow<NewSaleUiState> = combine(
        _cartItems,
        _selectedCustomer,
        _overallDiscount,
        _paymentMethod,
        combine(
            _paidAmount,
            _searchQuery,
            _searchResults,
            _isProcessing,
            combine(
                preferencesManager.defaultTaxPercent,
                preferencesManager.currencySymbol
            ) { tax, currency -> Pair(tax, currency) }
        ) { paid, query, results, processing, prefs ->
            TempSaleState(paid, query, results, processing, prefs.first, prefs.second)
        }
    ) { cart, customer, discount, method, temp ->
        val subtotal = cart.sumOf { it.lineTotal }
        val taxableAmount = subtotal - discount
        val taxAmount = taxableAmount * (temp.taxPercent / 100.0)
        val total = taxableAmount + taxAmount
        val paid = temp.paidAmount.toDoubleOrNull() ?: 0.0
        val change = if (method == PaymentMethod.CREDIT) 0.0 else (paid - total).coerceAtLeast(0.0)

        NewSaleUiState(
            cartItems = cart,
            selectedCustomer = customer,
            subtotal = subtotal,
            overallDiscount = discount,
            taxPercent = temp.taxPercent,
            taxAmount = taxAmount,
            total = total,
            paymentMethod = method,
            paidAmount = temp.paidAmount,
            changeAmount = change,
            searchQuery = temp.searchQuery,
            searchResults = temp.searchResults,
            isProcessing = temp.isProcessing,
            currencySymbol = temp.currencySymbol
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NewSaleUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            productRepository.searchProducts(query.trim()).collect { products ->
                _searchResults.value = products
            }
        }
    }

    fun addProductToCart(product: Product) {
        _cartItems.update { items ->
            val existing = items.indexOfFirst { it.product.id == product.id }
            if (existing >= 0) {
                items.toMutableList().apply {
                    val item = this[existing]
                    this[existing] = item.copy(quantity = item.quantity + 1.0)
                }
            } else {
                items + CartItem(product = product)
            }
        }
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    fun removeFromCart(productId: Long) {
        _cartItems.update { items -> items.filter { it.product.id != productId } }
    }

    fun updateQuantity(productId: Long, quantity: Double) {
        if (quantity <= 0) {
            removeFromCart(productId)
            return
        }
        _cartItems.update { items ->
            items.map { item ->
                if (item.product.id == productId) item.copy(quantity = quantity) else item
            }
        }
    }

    fun updateItemDiscount(productId: Long, discount: Double) {
        _cartItems.update { items ->
            items.map { item ->
                if (item.product.id == productId) item.copy(discount = discount.coerceAtLeast(0.0)) else item
            }
        }
    }

    fun applyDiscount(discount: Double) {
        _overallDiscount.value = discount.coerceAtLeast(0.0)
    }

    fun setPaymentMethod(method: PaymentMethod) {
        _paymentMethod.value = method
    }

    fun setPaidAmount(amount: String) {
        _paidAmount.value = amount
    }

    fun selectCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
    }

    fun completeSale() {
        val cart = _cartItems.value
        if (cart.isEmpty()) {
            viewModelScope.launch { _event.send(NewSaleEvent.ShowError("Cart is empty")) }
            return
        }

        val state = uiState.value
        val paid = state.paidAmount.toDoubleOrNull() ?: 0.0

        if (state.paymentMethod != PaymentMethod.CREDIT && paid < state.total) {
            viewModelScope.launch {
                _event.send(NewSaleEvent.ShowError("Paid amount must be at least ${state.currencySymbol} ${String.format("%.2f", state.total)}"))
            }
            return
        }

        if (state.paymentMethod == PaymentMethod.CREDIT && state.selectedCustomer == null) {
            viewModelScope.launch {
                _event.send(NewSaleEvent.ShowError("Please select a customer for credit sales"))
            }
            return
        }

        viewModelScope.launch {
            _isProcessing.value = true
            try {
                // Validate stock availability
                for (cartItem in cart) {
                    val product = productRepository.getProductById(cartItem.product.id)
                        ?: throw IllegalArgumentException("Product '${cartItem.product.name}' not found")
                    if (product.currentStock < cartItem.quantity) {
                        throw IllegalStateException(
                            "Insufficient stock for '${product.name}'. Available: ${product.currentStock}, Requested: ${cartItem.quantity}"
                        )
                    }
                }

                // Generate invoice number
                val invoiceNumber = generateInvoiceNumber()

                val invoiceStatus = if (state.paymentMethod == PaymentMethod.CREDIT) {
                    InvoiceStatus.CREDIT
                } else {
                    InvoiceStatus.COMPLETED
                }

                val invoiceItems = cart.map { cartItem ->
                    InvoiceItem(
                        productId = cartItem.product.id,
                        productName = cartItem.product.name,
                        quantity = cartItem.quantity,
                        unitPrice = cartItem.unitPrice,
                        discount = cartItem.discount,
                        totalPrice = cartItem.lineTotal
                    )
                }

                val invoice = Invoice(
                    invoiceNumber = invoiceNumber,
                    customerId = state.selectedCustomer?.id,
                    customerName = state.selectedCustomer?.name,
                    items = invoiceItems,
                    subtotal = state.subtotal,
                    discountAmount = state.overallDiscount,
                    taxPercent = state.taxPercent,
                    taxAmount = state.taxAmount,
                    totalAmount = state.total,
                    paidAmount = paid,
                    changeAmount = state.changeAmount,
                    paymentMethod = state.paymentMethod,
                    status = invoiceStatus
                )

                invoiceRepository.createInvoice(invoice)

                // Deduct stock and create adjustment entries
                for (cartItem in cart) {
                    val product = productRepository.getProductById(cartItem.product.id)!!
                    val newStock = product.currentStock - cartItem.quantity
                    productRepository.updateStock(cartItem.product.id, newStock)

                    stockAdjustmentRepository.addAdjustment(
                        StockAdjustment(
                            productId = cartItem.product.id,
                            productName = cartItem.product.name,
                            previousStock = product.currentStock,
                            newStock = newStock,
                            adjustmentQuantity = -cartItem.quantity,
                            reason = "Sale - Invoice $invoiceNumber",
                            type = AdjustmentType.SALE
                        )
                    )
                }

                // Update customer outstanding balance for credit sales
                if (invoiceStatus == InvoiceStatus.CREDIT && state.selectedCustomer != null) {
                    val unpaidAmount = state.total - paid
                    if (unpaidAmount > 0) {
                        val currentBalance = state.selectedCustomer.outstandingBalance
                        customerRepository.updateOutstandingBalance(
                            state.selectedCustomer.id,
                            currentBalance + unpaidAmount
                        )
                    }
                }

                // Reset cart state
                _cartItems.value = emptyList()
                _selectedCustomer.value = null
                _overallDiscount.value = 0.0
                _paymentMethod.value = PaymentMethod.CASH
                _paidAmount.value = ""

                _isProcessing.value = false
                _event.send(NewSaleEvent.SaleCompleted(invoiceNumber))
            } catch (e: Exception) {
                _isProcessing.value = false
                _event.send(NewSaleEvent.ShowError(e.message ?: "Failed to complete sale"))
            }
        }
    }

    private suspend fun generateInvoiceNumber(): String {
        val prefix = preferencesManager.invoicePrefix.first()
        val lastNumber = invoiceRepository.getLastInvoiceNumber()
        val nextNumber = if (lastNumber != null) {
            val numericPart = lastNumber.replace(Regex("[^0-9]"), "")
            (numericPart.toIntOrNull() ?: 0) + 1
        } else {
            preferencesManager.invoiceStartNumber.first()
        }
        return "$prefix${nextNumber.toString().padStart(5, '0')}"
    }
}

private data class TempSaleState(
    val paidAmount: String,
    val searchQuery: String,
    val searchResults: List<Product>,
    val isProcessing: Boolean,
    val taxPercent: Double,
    val currencySymbol: String
)
