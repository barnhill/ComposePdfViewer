package com.pnuema.android.pdfviewer

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pnuema.android.pdfviewer.fileretriever.DefaultFileRetriever
import com.pnuema.android.pdfviewer.fileretriever.PDFFileRetriever
import com.pnuema.android.pdfviewer.vm.PdfFileStateViewModel
import com.pnuema.android.pdfviewer.vm.PdfViewerViewModel
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import java.io.File

@Composable
fun PdfViewer(
    modifier: Modifier = Modifier,
    url: String,
    fileRetriever: PDFFileRetriever? = null,
    loadingContent: @Composable BoxScope.() -> Unit = {},
    options: PdfOptions = PdfOptions(),
    pageDivider: @Composable (BoxScope.() -> Unit)? = {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant
        )
    },
    onClick: ((Offset) -> Unit)? = null,
    onLongClick: ((Offset) -> Unit)? = null,
) {
    val context = LocalContext.current
    val viewModel = viewModel<PdfFileStateViewModel>{
        PdfFileStateViewModel(
            url = url,
            retriever = fileRetriever ?: DefaultFileRetriever(context)
        )
    }
    val fileState by viewModel.file.collectAsStateWithLifecycle()

    fileState?.let { file ->
        PdfViewer(
            modifier = modifier,
            file = file,
            options = options,
            pageDivider = pageDivider,
            onClick = onClick,
            onLongClick = onLongClick
        )
    } ?: run {
        Box(
            modifier
                .background(options.backgroundColor)
        ) {
            loadingContent()
        }
    }
}

@Composable
fun PdfViewer(
    modifier: Modifier = Modifier,
    file: File,
    options: PdfOptions = PdfOptions(removeFileWhenFinished = false),
    pageDivider: @Composable (BoxScope.() -> Unit)? = {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant
        )
    },
    onClick: ((Offset) -> Unit)? = null,
    onLongClick: ((Offset) -> Unit)? = null,
) {
    val context = LocalContext.current
    val viewModel = viewModel<PdfViewerViewModel> {
        PdfViewerViewModel(
            context = context,
            file = file,
            options = options,
        )
    }

    var zoomState = rememberZoomableState(ZoomSpec(maxZoomFactor = options.maxScale))
    val lazyColumnState = rememberLazyListState()
    val pageCount by viewModel.pageCount.collectAsStateWithLifecycle()
    val currentVisibleItems = lazyColumnState.currentVisibleItems()

    LaunchedEffect(
        key1 = currentVisibleItems + viewModel.isInited(),
        block = {
            if (viewModel.isInited()) {
                viewModel.generatePagesForVisibleItems(currentVisibleItems)
            }
        }
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(options.backgroundColor)
            .zoomable(
                state = zoomState,
                enabled = options.allowPinchToZoom,
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .onGloballyPositioned { coordinates ->
                viewModel.setSize(coordinates.size)
            },
        state = lazyColumnState,
        content = {
            items(pageCount) { pageIndex ->
                PdfPageComposable(
                    modifier = Modifier,
                    bitmap = viewModel.getPage(pageIndex),
                    pageIndex = pageIndex
                )

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    pageDivider?.invoke(this)
                }
            }
        }
    )
}

@Composable
private fun LazyListState.currentVisibleItems(): List<Int> = rememberSaveable(this) {
    derivedStateOf {
        when (layoutInfo.visibleItemsInfo.count()) {
            0 -> listOf(0)
            else -> {
                layoutInfo.visibleItemsInfo.map { it.index }
            }
        }
    }.value
}

@Composable
private fun PdfPageComposable(
    modifier: Modifier = Modifier,
    bitmap: Bitmap?,
    pageIndex: Int
) {
    if (bitmap != null) {
        Image(
            modifier = modifier
                .aspectRatio(1f)
                .clipToBounds()
                .fillMaxWidth(),
            bitmap = bitmap.asImageBitmap(),
            contentDescription = stringResource(R.string.document_page_content_description, pageIndex)
        )
    }
}