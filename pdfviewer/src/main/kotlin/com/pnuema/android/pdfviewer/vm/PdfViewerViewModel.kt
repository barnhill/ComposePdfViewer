package com.pnuema.android.pdfviewer.vm

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pnuema.android.pdfviewer.PdfOptions
import com.pnuema.android.pdfviewer.actions.PdfAction
import com.pnuema.android.pdfviewer.actions.PdfActionUtil
import com.pnuema.android.pdfviewer.generator.PdfBitmapGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.max
import kotlin.math.min

class PdfViewerViewModel(
    context: Context,
    private val file: File,
    private val options: PdfOptions,
): ViewModel() {
    private val _pageCount = MutableStateFlow<Int>(0)
    val pageCount = _pageCount.asStateFlow()

    private var pageSize = IntSize(1, 1)

    lateinit var pdfGenerator: PdfBitmapGenerator

    init {
        viewModelScope.launch {
            options.action.collect {
                handleAction(context, it)
            }
        }
    }

    fun isInited(): Boolean = ::pdfGenerator.isInitialized

    fun setSize(size: IntSize) {
        pageSize = size

        viewModelScope.launch(Dispatchers.Default) {
            pdfGenerator = PdfBitmapGenerator(file)
            _pageCount.emit(pdfGenerator.pageCount)
        }
    }

    fun generatePagesForVisibleItems(pages: List<Int>, size: IntSize = pageSize) {
        // 2 pages prior to first visible or 0
        val lowerPageBound = max(0, pages.first() - 2)

        // 2 pages after last visible or max page
        val upperPageBound = min(pdfGenerator.pageCount - 1, pages.last() + 2)

        for (i in lowerPageBound..upperPageBound) {
            pdfGenerator.getPdfBitmap(i, size)
        }
    }

    fun getPage(i: Int): Bitmap? = pdfGenerator.getPdfBitmap(i, pageSize)

    private fun handleAction(context: Context, action: PdfAction?) {
        when (action) {
            PdfAction.Print -> {
                PdfActionUtil.print(
                    context = context,
                    pdfGenerator = pdfGenerator,
                    size = pageSize
                )
            }

            PdfAction.Share -> {
                PdfActionUtil.share(context, file)
            }

            else -> Unit
        }

        options.clearAction()
    }

    override fun onCleared() {
        // release file descriptor in pdf generator
        pdfGenerator.close()

        // clean up temp file if needed
        if (options.removeFileWhenFinished) {
            try {
                file.delete()
            } catch (_: Exception) {}
        }

        super.onCleared()
    }
}