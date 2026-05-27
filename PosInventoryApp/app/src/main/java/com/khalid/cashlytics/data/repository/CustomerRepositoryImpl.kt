package com.khalid.cashlytics.data.repository

import com.khalid.cashlytics.data.local.dao.CustomerDao
import com.khalid.cashlytics.data.mapper.toDomain
import com.khalid.cashlytics.data.mapper.toEntity
import com.khalid.cashlytics.domain.model.Customer
import com.khalid.cashlytics.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CustomerRepositoryImpl @Inject constructor(
    private val customerDao: CustomerDao
) : CustomerRepository {

    override fun getAll(): Flow<List<Customer>> {
        return customerDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getById(id: Long): Customer? {
        return customerDao.getById(id)?.toDomain()
    }

    override fun search(query: String): Flow<List<Customer>> {
        return customerDao.search(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun add(customer: Customer): Long {
        return customerDao.insert(customer.toEntity())
    }

    override suspend fun update(customer: Customer) {
        customerDao.update(customer.toEntity())
    }

    override suspend fun delete(id: Long) {
        val entity = customerDao.getById(id) ?: return
        customerDao.delete(entity)
    }

    override suspend fun updateOutstandingBalance(id: Long, amount: Double) {
        customerDao.updateOutstandingBalance(id, amount)
    }

    override fun getCustomersWithOutstanding(): Flow<List<Customer>> {
        return customerDao.getCustomersWithOutstanding().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
