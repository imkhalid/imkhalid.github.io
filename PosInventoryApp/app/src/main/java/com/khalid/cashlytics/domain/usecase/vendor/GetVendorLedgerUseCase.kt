package com.khalid.cashlytics.domain.usecase.vendor

import com.khalid.cashlytics.domain.model.EntityType
import com.khalid.cashlytics.domain.model.PaymentRecord
import com.khalid.cashlytics.domain.model.Purchase
import com.khalid.cashlytics.domain.model.Vendor
import com.khalid.cashlytics.domain.repository.PaymentRepository
import com.khalid.cashlytics.domain.repository.PurchaseRepository
import com.khalid.cashlytics.domain.repository.VendorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class VendorLedger(
    val vendor: Vendor,
    val purchases: Flow<List<Purchase>>,
    val payments: Flow<List<PaymentRecord>>
)

class GetVendorLedgerUseCase @Inject constructor(
    private val vendorRepository: VendorRepository,
    private val purchaseRepository: PurchaseRepository,
    private val paymentRepository: PaymentRepository
) {
    suspend operator fun invoke(vendorId: Long): VendorLedger {
        require(vendorId > 0) { "Vendor ID must be valid" }

        val vendor = vendorRepository.getById(vendorId)
            ?: throw IllegalArgumentException("Vendor not found")

        return VendorLedger(
            vendor = vendor,
            purchases = purchaseRepository.getByVendor(vendorId),
            payments = paymentRepository.getByEntity(EntityType.VENDOR, vendorId)
        )
    }
}
