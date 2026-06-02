package com.khalid.vyntra.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.core.content.FileProvider
import com.khalid.vyntra.domain.model.Invoice
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Glue between [PdfGenerator] and Android's share / print frameworks.
 *
 * The generator writes the PDF to the app's cache directory; this helper
 * wraps the file in a [FileProvider] URI so external apps (Gmail, WhatsApp,
 * Telegram, Drive, the system print spooler …) can read it.
 */
object InvoiceShareHelper {

    private const val PDF_MIME = "application/pdf"
    private const val FILE_PROVIDER_AUTHORITY_SUFFIX = ".fileprovider"

    /**
     * Render [invoice] to a PDF on disk and launch a system "share" chooser
     * pre-populated with WhatsApp / Email / Telegram / Drive / etc.
     *
     * @return `true` if the chooser was launched; `false` if the PDF couldn't
     * be generated (caller should surface a snackbar).
     */
    fun shareInvoice(
        context: Context,
        invoice: Invoice,
        businessName: String,
        businessAddress: String,
        taxNumber: String,
        currencySymbol: String
    ): Boolean {
        val pdf = generatePdf(
            context, invoice, businessName, businessAddress, taxNumber, currencySymbol
        ) ?: return false

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}$FILE_PROVIDER_AUTHORITY_SUFFIX",
            pdf
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = PDF_MIME
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Invoice ${invoice.invoiceNumber}")
            putExtra(
                Intent.EXTRA_TEXT,
                buildString {
                    append("Invoice ${invoice.invoiceNumber}")
                    if (!invoice.customerName.isNullOrBlank()) {
                        append(" for ${invoice.customerName}")
                    }
                    append(".\n")
                    append("Total: ${invoice.totalAmount.toCurrency(currencySymbol)}")
                }
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Share invoice").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
        return true
    }

    /**
     * Render [invoice] to a PDF and submit it to the Android system print
     * spooler. The spooler handles printer discovery (Wi-Fi, Bluetooth,
     * cloud, USB depending on installed services).
     *
     * @return `true` if the print job was queued; `false` if the PDF couldn't
     * be generated or the device has no [PrintManager] (very old emulators).
     */
    fun printInvoice(
        context: Context,
        invoice: Invoice,
        businessName: String,
        businessAddress: String,
        taxNumber: String,
        currencySymbol: String
    ): Boolean {
        val pdf = generatePdf(
            context, invoice, businessName, businessAddress, taxNumber, currencySymbol
        ) ?: return false

        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            ?: return false

        val jobName = "Invoice ${invoice.invoiceNumber}"
        printManager.print(
            jobName,
            PdfFilePrintAdapter(jobName, pdf),
            PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(PrintAttributes.Resolution("default", "Default", 300, 300))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build()
        )
        return true
    }

    // ── PDF generation (shared by share + print) ───────────────────────────

    private fun generatePdf(
        context: Context,
        invoice: Invoice,
        businessName: String,
        businessAddress: String,
        taxNumber: String,
        currencySymbol: String
    ): File? = runCatching {
        PdfGenerator.generateInvoicePdf(
            context = context,
            invoice = invoice,
            businessName = businessName,
            businessAddress = businessAddress,
            taxNumber = taxNumber,
            currencySymbol = currencySymbol
        )
    }.getOrNull()

    /**
     * PrintDocumentAdapter that simply streams an already-generated PDF file
     * to the print spooler. The print framework will then route to whichever
     * printer the user picks (system, Wi-Fi, Bluetooth via OEM services, etc.).
     */
    private class PdfFilePrintAdapter(
        private val jobName: String,
        private val pdfFile: File
    ) : PrintDocumentAdapter() {

        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes,
            cancellationSignal: CancellationSignal,
            callback: LayoutResultCallback,
            extras: android.os.Bundle?
        ) {
            if (cancellationSignal.isCanceled) {
                callback.onLayoutCancelled()
                return
            }

            val info = PrintDocumentInfo.Builder("$jobName.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                // Our PDFs are single-page today; if PdfGenerator paginates
                // we'd report the real count instead.
                .setPageCount(1)
                .build()

            callback.onLayoutFinished(info, /* changed = */ true)
        }

        override fun onWrite(
            pages: Array<out PageRange>?,
            destination: ParcelFileDescriptor,
            cancellationSignal: CancellationSignal,
            callback: WriteResultCallback
        ) {
            try {
                FileInputStream(pdfFile).use { input ->
                    FileOutputStream(destination.fileDescriptor).use { output ->
                        val buffer = ByteArray(16 * 1024)
                        while (true) {
                            if (cancellationSignal.isCanceled) {
                                callback.onWriteCancelled()
                                return
                            }
                            val read = input.read(buffer)
                            if (read <= 0) break
                            output.write(buffer, 0, read)
                        }
                    }
                }
                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            } catch (e: Exception) {
                callback.onWriteFailed(e.message)
            }
        }
    }

    // Pre-existing API kept for future use — currently unused but documents
    // the public surface for tests.
    @Suppress("unused")
    fun ensurePrintAvailable(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
}
