package com.pnuema.android.pdfviewer.generator

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.createBitmap
import java.io.Closeable
import java.io.File

class PdfBitmapGenerator(file: File): Closeable {
    private val pdf = PdfRenderer(
        ParcelFileDescriptor.open(
            file, ParcelFileDescriptor.MODE_READ_ONLY
        )
    )

    private val cache = PdfPageCache()
    val pageCount: Int get() = pdf.pageCount

    fun getPdfBitmap(pageIndex: Int, size: IntSize): Bitmap? {
        return cache.get(pageIndex) ?: run {
            return@run generatePageBitmap(pageIndex, size)?.also {
                cache.put(pageIndex, it)
            }
        }
    }

    fun generatePageBitmap(pageIndex: Int, size: IntSize): Bitmap? {
        return pdf.openPage(pageIndex).use { page ->
            val widthMultiplier = (size.width.toFloat() / page.width.toFloat())

            val width = (page.width * widthMultiplier).toInt()
            val height = (page.height * widthMultiplier).toInt()

            if (width <= 0 || height <= 0) {
                return@use null
            }

            val pdfPageBitmap = createBitmap(width, height)

            try {
                page.render(
                    pdfPageBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT
                )
                Log.i(
                    "PdfViewer",
                    "Generated page {$pageIndex}"
                )
            } catch (e: Exception) {
                Log.e(
                    "PdfViewer",
                    "Generating failed for page {$pageIndex} :${e.message}"
                )
            }

            return pdfPageBitmap
        }
    }

    override fun close() {
        pdf.close()
    }
}

