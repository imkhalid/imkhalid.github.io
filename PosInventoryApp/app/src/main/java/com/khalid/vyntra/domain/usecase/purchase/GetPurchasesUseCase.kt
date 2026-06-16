package com.khalid.vyntra.domain.usecase.purchase

import com.khalid.vyntra.domain.model.Purchase
import com.khalid.vyntra.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPurchasesUseCase @Inject constructor(
    private val purchaseRepository: PurchaseRepository
) {
    operator fun invoke(): Flow<List<Purchase>> {
        return purchaseRepository.getAll()
    }

    fun byVendor(vendorId: Long): Flow<List<Purchase>> {
        return purchaseRepository.getByVendor(vendorId)
    }

    fun byDateRange(startDate: Long, endDate: Long): Flow<List<Purchase>> {
        return purchaseRepository.getByDateRange(startDate, endDate)
    }

    suspend fun byId(id: Long): Purchase? {
        return purchaseRepository.getById(id)
    }
}
