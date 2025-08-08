package com.pnuema.android.pdfviewer.print

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.pdf.PrintedPdfDocument
import android.util.Log
import androidx.compose.ui.unit.IntSize
import com.pnuema.android.pdfviewer.generator.PdfBitmapGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.io.IOException
import kotlin.system.measureTimeMillis

class PdfPrintDocumentAdapter(
    val context: Context,
    val pdfGenerator: PdfBitmapGenerator,
    val pageSize: IntSize,
): PrintDocumentAdapter() {
    var pdfDocument: PrintedPdfDocument? = null

    override fun onLayout(
        oldAttributes: PrintAttributes,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback,
        extras: Bundle?
    ) {
        pdfDocument = PrintedPdfDocument(context, newAttributes)
        // Respond to cancellation request
        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }

        // Compute the expected number of printed pages
        val pages = pdfGenerator.pageCount

        if (pages > 0) {
            PrintDocumentInfo.Builder("print_output.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(pages)
                .build()
                .also { info ->
                    // Content layout reflow is complete
                    callback.onLayoutFinished(info, true)
                }
        } else {
            // report an error to the print framework
            callback.onLayoutFailed("Page count calculation failed.")
        }
    }

    override fun onWrite(
        pages: Array<out PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback
    ) {
        CoroutineScope(Job() + Dispatchers.IO).launch {
            val doc = pdfDocument ?: return@launch

            val time = measureTimeMillis {
                for (range in pages) {
                    for (i in range.start..range.end) {
                        val image = pdfGenerator.generatePageBitmap(pageIndex = i, size = pageSize)
                        doc.startPage(i).also { page ->

                            // check for cancellation
                            if (cancellationSignal.isCanceled) {
                                callback.onWriteCancelled()
                                doc.finishPage(page)
                                doc.close()
                                pdfDocument = null
                                return@launch
                            }

                            // Draw page content for printing
                            drawPage(page, image)

                            // Rendering is complete, so page can be finalized.
                            doc.finishPage(page)
                        }
                    }
                }
            }

            Log.d("PdfViewer", "Printed image generation time: ${time}ms")

            // Write PDF document to file
            try {
                doc.writeTo(FileOutputStream(destination.fileDescriptor))
            } catch (e: IOException) {
                callback.onWriteFailed(e.toString())
                return@launch
            } finally {
                doc.close()
                pdfDocument = null
            }

            // Signal the print framework the document is complete
            callback.onWriteFinished(arrayOf<PageRange>(pages.first()))
        }
    }

    private fun drawPage(page: PdfDocument.Page, image: Bitmap?) {
        page.canvas.apply {
            (image ?: return).also {
                drawBitmap(it, null, page.info.contentRect, null)
            }
        }
    }
}