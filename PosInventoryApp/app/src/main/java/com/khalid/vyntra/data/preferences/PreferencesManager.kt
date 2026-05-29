package com.khalid.vyntra.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    // ── Preference Keys ─────────────────────────────────────────────────

    private object Keys {
        val BUSINESS_NAME = stringPreferencesKey("business_name")
        val BUSINESS_ADDRESS = stringPreferencesKey("business_address")
        val BUSINESS_LOGO = stringPreferencesKey("business_logo")
        val TAX_NUMBER = stringPreferencesKey("tax_number")
        val DEFAULT_TAX_PERCENT = doublePreferencesKey("default_tax_percent")
        val INVOICE_PREFIX = stringPreferencesKey("invoice_prefix")
        val INVOICE_START_NUMBER = intPreferencesKey("invoice_start_number")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val BACKUP_TIME = stringPreferencesKey("backup_time")
        val IS_PRO_USER = booleanPreferencesKey("is_pro_user")
        val INVENTORY_WRITE_COUNT = intPreferencesKey("inventory_write_count")
        val REPORT_VIEW_COUNT = intPreferencesKey("report_view_count")
        val LAST_RESET_DATE = longPreferencesKey("last_reset_date")
    }

    // ── Business Info ───────────────────────────────────────────────────

    val businessName: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.BUSINESS_NAME] ?: ""
    }

    suspend fun setBusinessName(name: String) {
        dataStore.edit { prefs -> prefs[Keys.BUSINESS_NAME] = name }
    }

    val businessAddress: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.BUSINESS_ADDRESS] ?: ""
    }

    suspend fun setBusinessAddress(address: String) {
        dataStore.edit { prefs -> prefs[Keys.BUSINESS_ADDRESS] = address }
    }

    val businessLogo: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.BUSINESS_LOGO] ?: ""
    }

    suspend fun setBusinessLogo(logoPath: String) {
        dataStore.edit { prefs -> prefs[Keys.BUSINESS_LOGO] = logoPath }
    }

    val taxNumber: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.TAX_NUMBER] ?: ""
    }

    suspend fun setTaxNumber(taxNumber: String) {
        dataStore.edit { prefs -> prefs[Keys.TAX_NUMBER] = taxNumber }
    }

    // ── Tax & Invoice Settings ──────────────────────────────────────────

    val defaultTaxPercent: Flow<Double> = dataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_TAX_PERCENT] ?: 0.0
    }

    suspend fun setDefaultTaxPercent(percent: Double) {
        dataStore.edit { prefs -> prefs[Keys.DEFAULT_TAX_PERCENT] = percent }
    }

    val invoicePrefix: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.INVOICE_PREFIX] ?: "INV-"
    }

    suspend fun setInvoicePrefix(prefix: String) {
        dataStore.edit { prefs -> prefs[Keys.INVOICE_PREFIX] = prefix }
    }

    val invoiceStartNumber: Flow<Int> = dataStore.data.map { prefs ->
        prefs[Keys.INVOICE_START_NUMBER] ?: 1
    }

    suspend fun setInvoiceStartNumber(number: Int) {
        dataStore.edit { prefs -> prefs[Keys.INVOICE_START_NUMBER] = number }
    }

    val currencySymbol: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.CURRENCY_SYMBOL] ?: "Rs"
    }

    suspend fun setCurrencySymbol(symbol: String) {
        dataStore.edit { prefs -> prefs[Keys.CURRENCY_SYMBOL] = symbol }
    }

    // ── App Settings ────────────────────────────────────────────────────

    val isDarkTheme: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.IS_DARK_THEME] ?: false
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.IS_DARK_THEME] = isDark }
    }

    val backupTime: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.BACKUP_TIME] ?: "02:00"
    }

    suspend fun setBackupTime(time: String) {
        dataStore.edit { prefs -> prefs[Keys.BACKUP_TIME] = time }
    }

    // ── Pro / Monetization ──────────────────────────────────────────────

    val isProUser: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.IS_PRO_USER] ?: false
    }

    suspend fun setProUser(isPro: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.IS_PRO_USER] = isPro }
    }

    val inventoryWriteCount: Flow<Int> = dataStore.data.map { prefs ->
        prefs[Keys.INVENTORY_WRITE_COUNT] ?: 0
    }

    suspend fun setInventoryWriteCount(count: Int) {
        dataStore.edit { prefs -> prefs[Keys.INVENTORY_WRITE_COUNT] = count }
    }

    suspend fun incrementInventoryWriteCount() {
        dataStore.edit { prefs ->
            val current = prefs[Keys.INVENTORY_WRITE_COUNT] ?: 0
            prefs[Keys.INVENTORY_WRITE_COUNT] = current + 1
        }
    }

    val reportViewCount: Flow<Int> = dataStore.data.map { prefs ->
        prefs[Keys.REPORT_VIEW_COUNT] ?: 0
    }

    suspend fun setReportViewCount(count: Int) {
        dataStore.edit { prefs -> prefs[Keys.REPORT_VIEW_COUNT] = count }
    }

    suspend fun incrementReportViewCount() {
        dataStore.edit { prefs ->
            val current = prefs[Keys.REPORT_VIEW_COUNT] ?: 0
            prefs[Keys.REPORT_VIEW_COUNT] = current + 1
        }
    }

    val lastResetDate: Flow<Long> = dataStore.data.map { prefs ->
        prefs[Keys.LAST_RESET_DATE] ?: 0L
    }

    suspend fun setLastResetDate(dateMillis: Long) {
        dataStore.edit { prefs -> prefs[Keys.LAST_RESET_DATE] = dateMillis }
    }

    // ── Bulk Reset (for daily free-tier counter resets) ─────────────────

    suspend fun resetDailyCounters() {
        dataStore.edit { prefs ->
            prefs[Keys.INVENTORY_WRITE_COUNT] = 0
            prefs[Keys.REPORT_VIEW_COUNT] = 0
            prefs[Keys.LAST_RESET_DATE] = System.currentTimeMillis()
        }
    }
}
