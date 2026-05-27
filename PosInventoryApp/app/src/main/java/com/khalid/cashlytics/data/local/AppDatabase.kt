package com.khalid.cashlytics.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.khalid.cashlytics.data.local.dao.CategoryDao
import com.khalid.cashlytics.data.local.dao.CustomerDao
import com.khalid.cashlytics.data.local.dao.InvoiceDao
import com.khalid.cashlytics.data.local.dao.InvoiceItemDao
import com.khalid.cashlytics.data.local.dao.PaymentRecordDao
import com.khalid.cashlytics.data.local.dao.ProductDao
import com.khalid.cashlytics.data.local.dao.PurchaseDao
import com.khalid.cashlytics.data.local.dao.PurchaseItemDao
import com.khalid.cashlytics.data.local.dao.StockAdjustmentDao
import com.khalid.cashlytics.data.local.dao.VendorDao
import com.khalid.cashlytics.data.local.entity.CategoryEntity
import com.khalid.cashlytics.data.local.entity.CustomerEntity
import com.khalid.cashlytics.data.local.entity.InvoiceEntity
import com.khalid.cashlytics.data.local.entity.InvoiceItemEntity
import com.khalid.cashlytics.data.local.entity.PaymentRecordEntity
import com.khalid.cashlytics.data.local.entity.ProductEntity
import com.khalid.cashlytics.data.local.entity.PurchaseEntity
import com.khalid.cashlytics.data.local.entity.PurchaseItemEntity
import com.khalid.cashlytics.data.local.entity.StockAdjustmentEntity
import com.khalid.cashlytics.data.local.entity.VendorEntity

@Database(
    entities = [
        ProductEntity::class,
        CategoryEntity::class,
        CustomerEntity::class,
        VendorEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class,
        PurchaseEntity::class,
        PurchaseItemEntity::class,
        StockAdjustmentEntity::class,
        PaymentRecordEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun customerDao(): CustomerDao
    abstract fun vendorDao(): VendorDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun invoiceItemDao(): InvoiceItemDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun purchaseItemDao(): PurchaseItemDao
    abstract fun stockAdjustmentDao(): StockAdjustmentDao
    abstract fun paymentRecordDao(): PaymentRecordDao

    companion object {
        const val DATABASE_NAME = "cashlytics_db"
    }
}
