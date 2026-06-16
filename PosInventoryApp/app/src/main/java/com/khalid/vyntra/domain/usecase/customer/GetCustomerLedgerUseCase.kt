package com.khalid.vyntra.domain.usecase.customer

import com.khalid.vyntra.domain.model.Customer
import com.khalid.vyntra.domain.model.EntityType
import com.khalid.vyntra.domain.model.Invoice
import com.khalid.vyntra.domain.model.PaymentRecord
import com.khalid.vyntra.domain.repository.CustomerRepository
import com.khalid.vyntra.domain.repository.InvoiceRepository
import com.khalid.vyntra.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class CustomerLedger(
    val customer: Customer,
    val invoices: Flow<List<Invoice>>,
    val payments: Flow<List<PaymentRecord>>
)

class GetCustomerLedgerUseCase @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val invoiceRepository: InvoiceRepository,
    private val paymentRepository: PaymentRepository
) {
    suspend operator fun invoke(customerId: Long): CustomerLedger {
        require(customerId > 0) { "Customer ID must be valid" }

        val customer = customerRepository.getById(customerId)
            ?: throw IllegalArgumentException("Customer not found")

        return CustomerLedger(
            customer = customer,
            invoices = invoiceRepository.getByCustomer(customerId),
            payments = paymentRepository.getByEntity(EntityType.CUSTOMER, customerId)
        )
    }

    fun getCustomersWithOutstanding(): Flow<List<Customer>> {
        return customerRepository.getCustomersWithOutstanding()
    }
}
