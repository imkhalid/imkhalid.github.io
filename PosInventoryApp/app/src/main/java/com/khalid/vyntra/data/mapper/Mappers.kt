package com.khalid.vyntra.data.mapper

import com.khalid.vyntra.data.local.entity.CategoryEntity
import com.khalid.vyntra.data.local.entity.CustomerEntity
import com.khalid.vyntra.data.local.entity.InvoiceEntity
import com.khalid.vyntra.data.local.entity.InvoiceItemEntity
import com.khalid.vyntra.data.local.entity.PaymentRecordEntity
import com.khalid.vyntra.data.local.entity.ProductEntity
import com.khalid.vyntra.data.local.entity.PurchaseEntity
import com.khalid.vyntra.data.local.entity.PurchaseItemEntity
import com.khalid.vyntra.data.local.entity.StockAdjustmentEntity
import com.khalid.vyntra.data.local.entity.VendorEntity
import com.khalid.vyntra.domain.model.AdjustmentType
import com.khalid.vyntra.domain.model.Category
import com.khalid.vyntra.domain.model.Customer
import com.khalid.vyntra.domain.model.EntityType
import com.khalid.vyntra.domain.model.Invoice
import com.khalid.vyntra.domain.model.InvoiceItem
import com.khalid.vyntra.domain.model.InvoiceStatus
import com.khalid.vyntra.domain.model.PaymentMethod
import com.khalid.vyntra.domain.model.PaymentRecord
import com.khalid.vyntra.domain.model.Product
import com.khalid.vyntra.domain.model.Purchase
import com.khalid.vyntra.domain.model.PurchaseItem
import com.khalid.vyntra.domain.model.StockAdjustment
import com.khalid.vyntra.domain.model.Vendor

// ── Product ─────────────────────────────────────────────────────────────────────

fun ProductEntity.toDomain(
    categoryName: String? = null,
    supplierName: String? = null
): Product = Product(
    id = id,
    name = name,
    sku = sku,
    barcode = barcode.ifEmpty { null },
    categoryId = categoryId,
    categoryName = categoryName,
    unit = unit,
    purchasePrice = purchasePrice,
    sellingPrice = sellingPrice,
    currentStock = currentStock.toDouble(),
    minStockThreshold = minStockThreshold.toDouble(),
    supplierId = supplierId,
    supplierName = supplierName,
    imagePath = imagePath,
    notes = notes.ifEmpty { null },
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    name = name,
    sku = sku,
    barcode = barcode.orEmpty(),
    categoryId = categoryId,
    unit = unit,
    purchasePrice = purchasePrice,
    sellingPrice = sellingPrice,
    currentStock = currentStock.toInt(),
    minStockThreshold = minStockThreshold.toInt(),
    supplierId = supplierId,
    imagePath = imagePath,
    notes = notes.orEmpty(),
    createdAt = createdAt,
    updatedAt = updatedAt
)

// ── Category ────────────────────────────────────────────────────────────────────

fun CategoryEntity.toDomain(productCount: Int = 0): Category = Category(
    id = id,
    name = name,
    description = description.ifEmpty { null },
    productCount = productCount,
    createdAt = createdAt
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    description = description.orEmpty(),
    createdAt = createdAt
)

// ── Customer ────────────────────────────────────────────────────────────────────

fun CustomerEntity.toDomain(): Customer = Customer(
    id = id,
    name = name,
    phone = phone.ifEmpty { null },
    email = email.ifEmpty { null },
    address = address.ifEmpty { null },
    cnicOrId = cnicOrId.ifEmpty { null },
    creditLimit = creditLimit,
    outstandingBalance = outstandingBalance,
    notes = notes.ifEmpty { null },
    createdAt = createdAt
)

fun Customer.toEntity(): CustomerEntity = CustomerEntity(
    id = id,
    name = name,
    phone = phone.orEmpty(),
    email = email.orEmpty(),
    address = address.orEmpty(),
    cnicOrId = cnicOrId.orEmpty(),
    creditLimit = creditLimit,
    outstandingBalance = outstandingBalance,
    notes = notes.orEmpty(),
    createdAt = createdAt
)

// ── Vendor ──────────────────────────────────────────────────────────────────────

fun VendorEntity.toDomain(): Vendor = Vendor(
    id = id,
    name = name,
    company = company.ifEmpty { null },
    phone = phone.ifEmpty { null },
    email = email.ifEmpty { null },
    address = address.ifEmpty { null },
    bankDetails = bankDetails.ifEmpty { null },
    currentPayableBalance = currentPayableBalance,
    notes = notes.ifEmpty { null },
    createdAt = createdAt
)

fun Vendor.toEntity(): VendorEntity = VendorEntity(
    id = id,
    name = name,
    company = company.orEmpty(),
    phone = phone.orEmpty(),
    email = email.orEmpty(),
    address = address.orEmpty(),
    bankDetails = bankDetails.orEmpty(),
    currentPayableBalance = currentPayableBalance,
    notes = notes.orEmpty(),
    createdAt = createdAt
)

// ── Invoice ─────────────────────────────────────────────────────────────────────

fun InvoiceEntity.toDomain(
    customerName: String? = null,
    items: List<InvoiceItem> = emptyList()
): Invoice = Invoice(
    id = id,
    invoiceNumber = invoiceNumber,
    customerId = customerId,
    customerName = customerName,
    items = items,
    subtotal = subtotal,
    discountAmount = discountAmount,
    taxPercent = taxPercent,
    taxAmount = taxAmount,
    totalAmount = totalAmount,
    paidAmount = paidAmount,
    changeAmount = changeAmount,
    paymentMethod = runCatching { PaymentMethod.valueOf(paymentMethod) }
        .getOrDefault(PaymentMethod.CASH),
    status = runCatching { InvoiceStatus.valueOf(status) }
        .getOrDefault(InvoiceStatus.COMPLETED),
    notes = notes.ifEmpty { null },
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Invoice.toEntity(): InvoiceEntity = InvoiceEntity(
    id = id,
    invoiceNumber = invoiceNumber,
    customerId = customerId,
    subtotal = subtotal,
    discountAmount = discountAmount,
    taxPercent = taxPercent,
    taxAmount = taxAmount,
    totalAmount = totalAmount,
    paidAmount = paidAmount,
    changeAmount = changeAmount,
    paymentMethod = paymentMethod.name,
    status = status.name,
    notes = notes.orEmpty(),
    createdAt = createdAt,
    updatedAt = updatedAt
)

// ── InvoiceItem ─────────────────────────────────────────────────────────────────

fun InvoiceItemEntity.toDomain(): InvoiceItem = InvoiceItem(
    id = id,
    invoiceId = invoiceId,
    productId = productId ?: 0L,
    productName = productName,
    quantity = quantity.toDouble(),
    unitPrice = unitPrice,
    discount = discount,
    totalPrice = totalPrice
)

fun InvoiceItem.toEntity(): InvoiceItemEntity = InvoiceItemEntity(
    id = id,
    invoiceId = invoiceId,
    productId = productId,
    productName = productName,
    quantity = quantity.toInt(),
    unitPrice = unitPrice,
    discount = discount,
    totalPrice = totalPrice
)

// ── Purchase ────────────────────────────────────────────────────────────────────

fun PurchaseEntity.toDomain(
    vendorName: String = "",
    items: List<PurchaseItem> = emptyList()
): Purchase = Purchase(
    id = id,
    vendorId = vendorId ?: 0L,
    vendorName = vendorName,
    items = items,
    totalAmount = totalAmount,
    notes = notes.ifEmpty { null },
    createdAt = createdAt
)

fun Purchase.toEntity(): PurchaseEntity = PurchaseEntity(
    id = id,
    vendorId = vendorId,
    totalAmount = totalAmount,
    notes = notes.orEmpty(),
    createdAt = createdAt
)

// ── PurchaseItem ────────────────────────────────────────────────────────────────

fun PurchaseItemEntity.toDomain(): PurchaseItem = PurchaseItem(
    id = id,
    purchaseId = purchaseId,
    productId = productId ?: 0L,
    productName = productName,
    quantity = quantity.toDouble(),
    costPrice = costPrice,
    totalPrice = totalPrice
)

fun PurchaseItem.toEntity(): PurchaseItemEntity = PurchaseItemEntity(
    id = id,
    purchaseId = purchaseId,
    productId = productId,
    productName = productName,
    quantity = quantity.toInt(),
    costPrice = costPrice,
    totalPrice = totalPrice
)

// ── StockAdjustment ─────────────────────────────────────────────────────────────

fun StockAdjustmentEntity.toDomain(productName: String = ""): StockAdjustment = StockAdjustment(
    id = id,
    productId = productId,
    productName = productName,
    previousStock = previousStock.toDouble(),
    newStock = newStock.toDouble(),
    adjustmentQuantity = adjustmentQuantity.toDouble(),
    reason = reason.ifEmpty { null },
    type = runCatching { AdjustmentType.valueOf(type) }
        .getOrDefault(AdjustmentType.MANUAL),
    createdAt = createdAt
)

fun StockAdjustment.toEntity(): StockAdjustmentEntity = StockAdjustmentEntity(
    id = id,
    productId = productId,
    previousStock = previousStock.toInt(),
    newStock = newStock.toInt(),
    adjustmentQuantity = adjustmentQuantity.toInt(),
    reason = reason.orEmpty(),
    type = type.name,
    createdAt = createdAt
)

// ── PaymentRecord ───────────────────────────────────────────────────────────────

fun PaymentRecordEntity.toDomain(entityName: String = ""): PaymentRecord = PaymentRecord(
    id = id,
    entityType = runCatching { EntityType.valueOf(entityType) }
        .getOrDefault(EntityType.CUSTOMER),
    entityId = entityId,
    entityName = entityName,
    amount = amount,
    paymentMethod = runCatching { PaymentMethod.valueOf(paymentMethod) }
        .getOrDefault(PaymentMethod.CASH),
    notes = notes.ifEmpty { null },
    createdAt = createdAt
)

fun PaymentRecord.toEntity(): PaymentRecordEntity = PaymentRecordEntity(
    id = id,
    entityType = entityType.name,
    entityId = entityId,
    amount = amount,
    paymentMethod = paymentMethod.name,
    notes = notes.orEmpty(),
    createdAt = createdAt
)
