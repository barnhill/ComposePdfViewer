package com.pnuema.android.pdfviewer.generator

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.createBitmap
import java.io.Closeable
import java.io.File
import kotlin.system.measureTimeMillis

class PdfBitmapGenerator @WorkerThread constructor(file: File): Closeable {
    private val cache = PdfPageCache()
    private val pdf = PdfRenderer(
        ParcelFileDescriptor.open(
            file,
            ParcelFileDescriptor.MODE_READ_ONLY
        )
    )
    val pageCount: Int get() = pdf.pageCount

    init {
        Log.d("PdfViewer", "Generator created")
    }

    fun getPdfBitmap(pageIndex: Int, size: IntSize): Bitmap? = cache.get(pageIndex) ?: run {
        return@run generatePageBitmap(pageIndex, size)?.also {
            cache.put(pageIndex, it)
        }
    }

    fun generatePageBitmap(pageIndex: Int, size: IntSize): Bitmap? = pdf.openPage(pageIndex).use {
        val widthMultiplier = (size.width.toFloat() / it.width.toFloat())

        val width = (it.width * widthMultiplier).toInt()
        val height = (it.height * widthMultiplier).toInt()

        if (width <= 0 || height <= 0) {
            return@use null
        }

        val pdfPageBitmap = createBitmap(width, height)

        try {
            val time = measureTimeMillis {
                it.render(pdfPageBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
            }
            Log.d("PdfViewer", "Generated page $pageIndex in ${time}ms")
        } catch (e: Exception) {
            Log.e("PdfViewer", "Generating failed for page {$pageIndex} :${e.message}")
        }

        pdfPageBitmap
    }

    override fun close() {
        pdf.close()
    }
}

