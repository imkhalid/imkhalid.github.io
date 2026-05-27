package com.khalid.cashlytics.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.util.Log
import com.khalid.cashlytics.domain.model.Invoice
import com.khalid.cashlytics.domain.model.InvoiceItem
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Generates PDF invoices using Android's [PdfDocument] API.
 */
object PdfGenerator {

    private const val TAG = "PdfGenerator"

    // A4 dimensions in PostScript points (72 points per inch)
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    private const val MARGIN_LEFT = 40f
    private const val MARGIN_RIGHT = 40f
    private const val MARGIN_TOP = 40f
    private const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT

    // Column positions for the line-items table
    private const val COL_ITEM = MARGIN_LEFT
    private const val COL_QTY = 330f
    private const val COL_PRICE = 400f
    private const val COL_DISCOUNT = 460f
    private const val COL_TOTAL = 520f

    /**
     * Generates a PDF invoice file and returns it.
     *
     * @param context          Android context (used for cache directory access).
     * @param invoice          The invoice data including line items.
     * @param businessName     Name of the business to show in the header.
     * @param businessAddress  Address of the business.
     * @param taxNumber        Tax registration number (if any).
     * @param currencySymbol   Currency symbol for monetary values (e.g. "Rs").
     * @return The generated PDF [File], or `null` if generation failed.
     */
    fun generateInvoicePdf(
        context: Context,
        invoice: Invoice,
        businessName: String,
        businessAddress: String,
        taxNumber: String,
        currencySymbol: String
    ): File {
        val document = PdfDocument()

        try {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            var yPos = MARGIN_TOP

            // ── Business Header ────────────────────────────────────────────
            yPos = drawBusinessHeader(canvas, yPos, businessName, businessAddress, taxNumber)
            yPos += 10f

            // ── Divider ────────────────────────────────────────────────────
            yPos = drawDivider(canvas, yPos)
            yPos += 15f

            // ── Invoice Details ────────────────────────────────────────────
            yPos = drawInvoiceDetails(canvas, yPos, invoice, currencySymbol)
            yPos += 15f

            // ── Divider ────────────────────────────────────────────────────
            yPos = drawDivider(canvas, yPos)
            yPos += 10f

            // ── Line Items Table ───────────────────────────────────────────
            yPos = drawTableHeader(canvas, yPos)
            yPos = drawDivider(canvas, yPos)
            yPos += 5f

            for (item in invoice.items) {
                // Check if we need a new page (leave room for totals)
                if (yPos > PAGE_HEIGHT - 180f) {
                    // For simplicity, we truncate items that don't fit on a single page.
                    // A production app with many items would start a new page here.
                    break
                }
                yPos = drawLineItem(canvas, yPos, item, currencySymbol)
            }
            yPos += 5f
            yPos = drawDivider(canvas, yPos)
            yPos += 10f

            // ── Totals Section ─────────────────────────────────────────────
            yPos = drawTotals(canvas, yPos, invoice, currencySymbol)
            yPos += 15f

            // ── Payment Info ───────────────────────────────────────────────
            yPos = drawDivider(canvas, yPos)
            yPos += 10f
            yPos = drawPaymentInfo(canvas, yPos, invoice, currencySymbol)

            // ── Footer ─────────────────────────────────────────────────────
            drawFooter(canvas)

            document.finishPage(page)

            // ── Save to file ───────────────────────────────────────────────
            val dir = File(context.cacheDir, "invoices")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val file = File(dir, "${invoice.invoiceNumber}.pdf")
            FileOutputStream(file).use { outputStream ->
                document.writeTo(outputStream)
            }
            return file

        } catch (e: IOException) {
            Log.e(TAG, "Failed to generate invoice PDF", e)
            throw e
        } finally {
            document.close()
        }
    }

    // ── Private drawing helpers ────────────────────────────────────────────────

    private fun drawBusinessHeader(
        canvas: Canvas,
        startY: Float,
        businessName: String,
        businessAddress: String,
        taxNumber: String
    ): Float {
        var y = startY

        val titlePaint = Paint().apply {
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(businessName.ifEmpty { "Business" }, MARGIN_LEFT, y + 20f, titlePaint)
        y += 28f

        val normalPaint = Paint().apply {
            textSize = 10f
            isAntiAlias = true
            color = android.graphics.Color.DKGRAY
        }
        if (businessAddress.isNotEmpty()) {
            canvas.drawText(businessAddress, MARGIN_LEFT, y + 12f, normalPaint)
            y += 16f
        }
        if (taxNumber.isNotEmpty()) {
            canvas.drawText("Tax No: $taxNumber", MARGIN_LEFT, y + 12f, normalPaint)
            y += 16f
        }

        return y
    }

    private fun drawDivider(canvas: Canvas, y: Float): Float {
        val linePaint = Paint().apply {
            strokeWidth = 0.5f
            color = android.graphics.Color.GRAY
            isAntiAlias = true
        }
        canvas.drawLine(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y, linePaint)
        return y + 2f
    }

    private fun drawInvoiceDetails(
        canvas: Canvas,
        startY: Float,
        invoice: Invoice,
        currencySymbol: String
    ): Float {
        var y = startY

        val labelPaint = Paint().apply {
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            textSize = 10f
            isAntiAlias = true
        }

        val lineHeight = 16f

        // Left column
        canvas.drawText("Invoice #:", MARGIN_LEFT, y + 12f, labelPaint)
        canvas.drawText(invoice.invoiceNumber, MARGIN_LEFT + 70f, y + 12f, valuePaint)

        // Right column
        canvas.drawText("Date:", 350f, y + 12f, labelPaint)
        canvas.drawText(invoice.createdAt.toFormattedDateTime(), 385f, y + 12f, valuePaint)
        y += lineHeight

        canvas.drawText("Status:", MARGIN_LEFT, y + 12f, labelPaint)
        canvas.drawText(invoice.status.name, MARGIN_LEFT + 70f, y + 12f, valuePaint)

        canvas.drawText("Payment:", 350f, y + 12f, labelPaint)
        canvas.drawText(invoice.paymentMethod.name, 400f, y + 12f, valuePaint)
        y += lineHeight

        if (!invoice.customerName.isNullOrEmpty()) {
            canvas.drawText("Customer:", MARGIN_LEFT, y + 12f, labelPaint)
            canvas.drawText(invoice.customerName, MARGIN_LEFT + 70f, y + 12f, valuePaint)
            y += lineHeight
        }

        return y
    }

    private fun drawTableHeader(canvas: Canvas, startY: Float): Float {
        val headerPaint = Paint().apply {
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val y = startY + 14f
        canvas.drawText("Item", COL_ITEM, y, headerPaint)
        canvas.drawText("Qty", COL_QTY, y, headerPaint)
        canvas.drawText("Price", COL_PRICE, y, headerPaint)
        canvas.drawText("Disc.", COL_DISCOUNT, y, headerPaint)
        canvas.drawText("Total", COL_TOTAL, y, headerPaint)

        return startY + 20f
    }

    private fun drawLineItem(
        canvas: Canvas,
        startY: Float,
        item: InvoiceItem,
        currencySymbol: String
    ): Float {
        val itemPaint = Paint().apply {
            textSize = 9f
            isAntiAlias = true
        }

        val y = startY + 12f

        // Truncate long product names
        val name = if (item.productName.length > 40) {
            item.productName.take(37) + "..."
        } else {
            item.productName
        }

        canvas.drawText(name, COL_ITEM, y, itemPaint)
        canvas.drawText(formatQty(item.quantity), COL_QTY, y, itemPaint)
        canvas.drawText(formatMoney(item.unitPrice, currencySymbol), COL_PRICE, y, itemPaint)
        canvas.drawText(formatMoney(item.discount, currencySymbol), COL_DISCOUNT, y, itemPaint)
        canvas.drawText(formatMoney(item.totalPrice, currencySymbol), COL_TOTAL, y, itemPaint)

        return startY + 16f
    }

    private fun drawTotals(
        canvas: Canvas,
        startY: Float,
        invoice: Invoice,
        currencySymbol: String
    ): Float {
        var y = startY

        val labelPaint = Paint().apply {
            textSize = 10f
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            textSize = 10f
            isAntiAlias = true
        }
        val totalLabelPaint = Paint().apply {
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val totalValuePaint = Paint().apply {
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val labelX = 380f
        val valueX = COL_TOTAL
        val lineHeight = 16f

        canvas.drawText("Subtotal:", labelX, y + 12f, labelPaint)
        canvas.drawText(formatMoney(invoice.subtotal, currencySymbol), valueX, y + 12f, valuePaint)
        y += lineHeight

        if (invoice.discountAmount > 0) {
            canvas.drawText("Discount:", labelX, y + 12f, labelPaint)
            canvas.drawText("-${formatMoney(invoice.discountAmount, currencySymbol)}", valueX, y + 12f, valuePaint)
            y += lineHeight
        }

        if (invoice.taxAmount > 0) {
            canvas.drawText("Tax (${invoice.taxPercent}%):", labelX, y + 12f, labelPaint)
            canvas.drawText(formatMoney(invoice.taxAmount, currencySymbol), valueX, y + 12f, valuePaint)
            y += lineHeight
        }

        y += 4f
        drawDivider(canvas, y)
        y += 6f

        canvas.drawText("Total:", labelX, y + 14f, totalLabelPaint)
        canvas.drawText(formatMoney(invoice.totalAmount, currencySymbol), valueX, y + 14f, totalValuePaint)
        y += 20f

        return y
    }

    private fun drawPaymentInfo(
        canvas: Canvas,
        startY: Float,
        invoice: Invoice,
        currencySymbol: String
    ): Float {
        var y = startY

        val labelPaint = Paint().apply {
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            textSize = 10f
            isAntiAlias = true
        }
        val lineHeight = 16f

        canvas.drawText("Paid:", MARGIN_LEFT, y + 12f, labelPaint)
        canvas.drawText(formatMoney(invoice.paidAmount, currencySymbol), MARGIN_LEFT + 80f, y + 12f, valuePaint)
        y += lineHeight

        if (invoice.changeAmount > 0) {
            canvas.drawText("Change:", MARGIN_LEFT, y + 12f, labelPaint)
            canvas.drawText(formatMoney(invoice.changeAmount, currencySymbol), MARGIN_LEFT + 80f, y + 12f, valuePaint)
            y += lineHeight
        }

        val balance = invoice.totalAmount - invoice.paidAmount
        if (balance > 0.01) {
            canvas.drawText("Balance Due:", MARGIN_LEFT, y + 12f, labelPaint)
            canvas.drawText(formatMoney(balance, currencySymbol), MARGIN_LEFT + 80f, y + 12f, valuePaint)
            y += lineHeight
        }

        if (!invoice.notes.isNullOrEmpty()) {
            y += 8f
            canvas.drawText("Notes:", MARGIN_LEFT, y + 12f, labelPaint)
            y += lineHeight
            canvas.drawText(invoice.notes, MARGIN_LEFT, y + 12f, valuePaint)
            y += lineHeight
        }

        return y
    }

    private fun drawFooter(canvas: Canvas) {
        val footerPaint = Paint().apply {
            textSize = 8f
            isAntiAlias = true
            color = android.graphics.Color.GRAY
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Generated by Cashlytics",
            PAGE_WIDTH / 2f,
            PAGE_HEIGHT - 20f,
            footerPaint
        )
    }

    // ── Formatting helpers ─────────────────────────────────────────────────────

    private fun formatMoney(value: Double, symbol: String): String {
        return value.toCurrency(symbol)
    }

    private fun formatQty(quantity: Double): String {
        return if (quantity == quantity.toLong().toDouble()) {
            quantity.toLong().toString()
        } else {
            String.format("%.2f", quantity)
        }
    }
}
