package com.khalid.vyntra.data.repository

import com.khalid.vyntra.data.local.dao.VendorDao
import com.khalid.vyntra.data.mapper.toDomain
import com.khalid.vyntra.data.mapper.toEntity
import com.khalid.vyntra.domain.model.Vendor
import com.khalid.vyntra.domain.repository.VendorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class VendorRepositoryImpl @Inject constructor(
    private val vendorDao: VendorDao
) : VendorRepository {

    override fun getAll(): Flow<List<Vendor>> {
        return vendorDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getById(id: Long): Vendor? {
        return vendorDao.getById(id)?.toDomain()
    }

    override fun search(query: String): Flow<List<Vendor>> {
        return vendorDao.search(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun add(vendor: Vendor): Long {
        return vendorDao.insert(vendor.toEntity())
    }

    override suspend fun update(vendor: Vendor) {
        vendorDao.update(vendor.toEntity())
    }

    override suspend fun delete(id: Long) {
        val entity = vendorDao.getById(id) ?: return
        vendorDao.delete(entity)
    }

    override suspend fun updatePayableBalance(id: Long, amount: Double) {
        vendorDao.updatePayableBalance(id, amount)
    }
}
