package com.khalid.cashlytics.domain.usecase.vendor

import com.khalid.cashlytics.domain.model.Vendor
import com.khalid.cashlytics.domain.repository.VendorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageVendorUseCase @Inject constructor(
    private val vendorRepository: VendorRepository
) {
    fun getAll(): Flow<List<Vendor>> {
        return vendorRepository.getAll()
    }

    suspend fun getById(id: Long): Vendor? {
        return vendorRepository.getById(id)
    }

    fun search(query: String): Flow<List<Vendor>> {
        return vendorRepository.search(query.trim())
    }

    suspend fun add(vendor: Vendor): Long {
        require(vendor.name.isNotBlank()) { "Vendor name cannot be empty" }
        return vendorRepository.add(vendor)
    }

    suspend fun update(vendor: Vendor) {
        require(vendor.id > 0) { "Vendor ID must be valid" }
        require(vendor.name.isNotBlank()) { "Vendor name cannot be empty" }
        vendorRepository.update(vendor)
    }

    suspend fun delete(id: Long) {
        require(id > 0) { "Vendor ID must be valid" }
        vendorRepository.delete(id)
    }
}
