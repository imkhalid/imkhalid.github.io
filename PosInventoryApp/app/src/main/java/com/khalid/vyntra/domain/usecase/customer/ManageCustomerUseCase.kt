package com.khalid.vyntra.domain.usecase.customer

import com.khalid.vyntra.domain.model.Customer
import com.khalid.vyntra.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageCustomerUseCase @Inject constructor(
    private val customerRepository: CustomerRepository
) {
    fun getAll(): Flow<List<Customer>> {
        return customerRepository.getAll()
    }

    suspend fun getById(id: Long): Customer? {
        return customerRepository.getById(id)
    }

    fun search(query: String): Flow<List<Customer>> {
        return customerRepository.search(query.trim())
    }

    suspend fun add(customer: Customer): Long {
        require(customer.name.isNotBlank()) { "Customer name cannot be empty" }
        require(customer.creditLimit >= 0) { "Credit limit cannot be negative" }
        return customerRepository.add(customer)
    }

    suspend fun update(customer: Customer) {
        require(customer.id > 0) { "Customer ID must be valid" }
        require(customer.name.isNotBlank()) { "Customer name cannot be empty" }
        customerRepository.update(customer)
    }

    suspend fun delete(id: Long) {
        require(id > 0) { "Customer ID must be valid" }
        customerRepository.delete(id)
    }
}
