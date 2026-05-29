package com.khalid.vyntra.util

import com.khalid.vyntra.domain.model.Invoice
import com.khalid.vyntra.domain.model.InvoiceStatus
import com.khalid.vyntra.domain.model.PaymentMethod
import com.khalid.vyntra.domain.model.Product
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter

/**
 * Handles CSV import/export for products, invoices, and generic reports
 * using Apache Commons CSV.
 */
object CsvManager {

    // ── Product Export ──────────────────────────────────────────────────────────

    private val productHeaders = arrayOf(
        "ID", "Name", "SKU", "Barcode", "Category ID", "Unit",
        "Purchase Price", "Selling Price", "Current Stock",
        "Min Stock Threshold", "Supplier ID", "Notes"
    )

    /**
     * Exports a list of [Product] objects to CSV format.
     *
     * @param products      The products to export.
     * @param outputStream  The output stream to write the CSV data to.
     */
    fun exportProducts(products: List<Product>, outputStream: OutputStream) {
        val writer = OutputStreamWriter(outputStream, Charsets.UTF_8)
        val format = CSVFormat.DEFAULT.builder()
            .setHeader(*productHeaders)
            .build()

        CSVPrinter(writer, format).use { printer ->
            for (product in products) {
                printer.printRecord(
                    product.id,
                    product.name,
                    product.sku,
                    product.barcode.orEmpty(),
                    product.categoryId ?: "",
                    product.unit,
                    product.purchasePrice,
                    product.sellingPrice,
                    product.currentStock,
                    product.minStockThreshold,
                    product.supplierId ?: "",
                    product.notes.orEmpty()
                )
            }
        }
    }

    // ── Product Import ─────────────────────────────────────────────────────────

    /**
     * Imports products from a CSV input stream.
     * Expects the same headers as [exportProducts] produces.
     * Rows with missing or invalid required fields are skipped.
     *
     * @param inputStream  The input stream containing CSV data.
     * @return A list of parsed [Product] objects.
     */
    fun importProducts(inputStream: InputStream): List<Product> {
        val reader = InputStreamReader(inputStream, Charsets.UTF_8)
        val format = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreEmptyLines(true)
            .setTrim(true)
            .build()

        val products = mutableListOf<Product>()

        CSVParser(reader, format).use { parser ->
            for (record in parser) {
                try {
                    val product = Product(
                        id = record.get("ID").toLongOrNull() ?: 0L,
                        name = record.get("Name").takeIf { it.isNotBlank() }
                            ?: continue,
                        sku = record.get("SKU"),
                        barcode = record.get("Barcode").takeIf { it.isNotBlank() },
                        categoryId = record.get("Category ID").toLongOrNull(),
                        unit = record.get("Unit"),
                        purchasePrice = record.get("Purchase Price").toDoubleOrNull() ?: 0.0,
                        sellingPrice = record.get("Selling Price").toDoubleOrNull() ?: 0.0,
                        currentStock = record.get("Current Stock").toDoubleOrNull() ?: 0.0,
                        minStockThreshold = record.get("Min Stock Threshold").toDoubleOrNull() ?: 0.0,
                        supplierId = record.get("Supplier ID").toLongOrNull(),
                        notes = record.get("Notes").takeIf { it.isNotBlank() }
                    )
                    products.add(product)
                } catch (e: Exception) {
                    // Skip malformed rows silently; caller can compare
                    // imported count vs expected count to detect issues.
                    continue
                }
            }
        }

        return products
    }

    // ── Invoice Export ──────────────────────────────────────────────────────────

    private val invoiceHeaders = arrayOf(
        "Invoice Number", "Customer Name", "Subtotal", "Discount",
        "Tax %", "Tax Amount", "Total", "Paid", "Change",
        "Payment Method", "Status", "Date", "Notes"
    )

    /**
     * Exports a list of [Invoice] objects to CSV format.
     *
     * @param invoices      The invoices to export.
     * @param outputStream  The output stream to write the CSV data to.
     */
    fun exportInvoices(invoices: List<Invoice>, outputStream: OutputStream) {
        val writer = OutputStreamWriter(outputStream, Charsets.UTF_8)
        val format = CSVFormat.DEFAULT.builder()
            .setHeader(*invoiceHeaders)
            .build()

        CSVPrinter(writer, format).use { printer ->
            for (invoice in invoices) {
                printer.printRecord(
                    invoice.invoiceNumber,
                    invoice.customerName.orEmpty(),
                    invoice.subtotal,
                    invoice.discountAmount,
                    invoice.taxPercent,
                    invoice.taxAmount,
                    invoice.totalAmount,
                    invoice.paidAmount,
                    invoice.changeAmount,
                    invoice.paymentMethod.name,
                    invoice.status.name,
                    invoice.createdAt.toFormattedDateTime(),
                    invoice.notes.orEmpty()
                )
            }
        }
    }

    // ── Generic Report Export ───────────────────────────────────────────────────

    /**
     * Exports a generic report (arbitrary headers and rows) to CSV format.
     *
     * @param headers       Column headers for the report.
     * @param rows          Row data — each inner list must match the header count.
     * @param outputStream  The output stream to write the CSV data to.
     */
    fun exportReport(
        headers: List<String>,
        rows: List<List<String>>,
        outputStream: OutputStream
    ) {
        val writer = OutputStreamWriter(outputStream, Charsets.UTF_8)
        val format = CSVFormat.DEFAULT.builder()
            .setHeader(*headers.toTypedArray())
            .build()

        CSVPrinter(writer, format).use { printer ->
            for (row in rows) {
                printer.printRecord(row)
            }
        }
    }
}
