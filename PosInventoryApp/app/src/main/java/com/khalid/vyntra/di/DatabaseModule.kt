package com.khalid.vyntra.di

import android.content.Context
import androidx.room.Room
import com.khalid.vyntra.data.local.AppDatabase
import com.khalid.vyntra.data.local.dao.CategoryDao
import com.khalid.vyntra.data.local.dao.CustomerDao
import com.khalid.vyntra.data.local.dao.InvoiceDao
import com.khalid.vyntra.data.local.dao.InvoiceItemDao
import com.khalid.vyntra.data.local.dao.PaymentRecordDao
import com.khalid.vyntra.data.local.dao.ProductDao
import com.khalid.vyntra.data.local.dao.PurchaseDao
import com.khalid.vyntra.data.local.dao.PurchaseItemDao
import com.khalid.vyntra.data.local.dao.StockAdjustmentDao
import com.khalid.vyntra.data.local.dao.VendorDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao {
        return database.productDao()
    }

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun provideCustomerDao(database: AppDatabase): CustomerDao {
        return database.customerDao()
    }

    @Provides
    fun provideVendorDao(database: AppDatabase): VendorDao {
        return database.vendorDao()
    }

    @Provides
    fun provideInvoiceDao(database: AppDatabase): InvoiceDao {
        return database.invoiceDao()
    }

    @Provides
    fun provideInvoiceItemDao(database: AppDatabase): InvoiceItemDao {
        return database.invoiceItemDao()
    }

    @Provides
    fun providePurchaseDao(database: AppDatabase): PurchaseDao {
        return database.purchaseDao()
    }

    @Provides
    fun providePurchaseItemDao(database: AppDatabase): PurchaseItemDao {
        return database.purchaseItemDao()
    }

    @Provides
    fun provideStockAdjustmentDao(database: AppDatabase): StockAdjustmentDao {
        return database.stockAdjustmentDao()
    }

    @Provides
    fun providePaymentRecordDao(database: AppDatabase): PaymentRecordDao {
        return database.paymentRecordDao()
    }
}
