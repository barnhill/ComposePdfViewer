package com.pnuema.android.pdfviewer

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.File

class PdfBitmapGenerator(file: File) {
    private val pdf = PdfRenderer(
        ParcelFileDescriptor.open(
            file,
            ParcelFileDescriptor.MODE_READ_ONLY
        )
    )

    val cache = PdfPageCache()
    val pageCount: Int get() = pdf.pageCount

    private val _pageCacheFlow: MutableSharedFlow<PdfPageCache> = MutableSharedFlow(0)
    val pageCacheFlow: SharedFlow<PdfPageCache> = _pageCacheFlow.asSharedFlow()

    suspend fun getPdfBitmap(pageIndex: Int, size: IntSize) {
        cache.get(pageIndex.toString()) ?: run {
            pdf.openPage(pageIndex).use { page ->
                val widthMultiplier = size.width / page.width

                val width = page.width * widthMultiplier
                val height = page.height * widthMultiplier

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
                    cache.put(pageIndex.toString(), pdfPageBitmap)
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