package com.khalid.cashlytics.domain.model

enum class PaymentMethod {
    CASH,
    CARD,
    CREDIT,
    SPLIT
}

enum class InvoiceStatus {
    COMPLETED,
    CANCELLED,
    CREDIT
}

enum class AdjustmentType {
    MANUAL,
    DAMAGE,
    EXPIRED,
    RETURN,
    SALE,
    PURCHASE
}

enum class EntityType {
    CUSTOMER,
    VENDOR
}
