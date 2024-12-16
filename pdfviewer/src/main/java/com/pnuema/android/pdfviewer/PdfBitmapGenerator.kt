package com.pnuema.android.pdfviewer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.File

class PdfBitmapGenerator(file: File, context: Context, cachePercent: Double = 0.15) {
    private val pdf = PdfRenderer(
        ParcelFileDescriptor.open(
            file,
            ParcelFileDescriptor.MODE_READ_ONLY
        )
    )

    val cache = PdfPageCache(context, cachePercent)
    val pageCount: Int get() = pdf.pageCount

    private val _pageCacheFlow: MutableSharedFlow<PdfPageCache> = MutableSharedFlow(0)
    val pageCacheFlow: SharedFlow<PdfPageCache> = _pageCacheFlow.asSharedFlow()

    suspend fun getPdfBitmap(pageIndex: Int, size: IntSize) {
        cache.get(pageIndex) ?: run {
            pdf.openPage(pageIndex).use { page ->
                val widthMultiplier = size.width.toFloat() / page.width.toFloat()

                val width = (page.width * widthMultiplier).toInt()
                val height = (page.height * widthMultiplier).toInt()

                if (width <= 0 || height <= 0) {
                    return@run
                }

                val pdfPageBitmap = Bitmap.createBitmap(
                    width,
                    height,
                    Bitmap.Config.ARGB_8888
                )

                try {
                    page.render(
                        pdfPageBitmap,
                        null,
                        null,
                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                    )
                    cache.put(pageIndex, pdfPageBitmap)
                } catch (e: Exception) {
                    Log.e(
                        "PdfViewer",
                        "PdfPageGenerationTask failed for index {$pageIndex} :${e.message}"
                    )
                }
            }

            _pageCacheFlow.emit(cache)
        }
    }
}