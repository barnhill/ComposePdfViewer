package com.pnuema.android.pdfviewer

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
            color = MaterialTheme.colorScheme.outline
        )
    },
    fetchFailedContent: @Composable (BoxScope.() -> Unit)? = {
        Text(
            text = "Failed to load",
            color = Color.Black
        )
    },
    onClick: ((Offset) -> Unit)? = null,
    onLongClick: ((Offset) -> Unit)? = null,
) {
    val context = LocalContext.current
    val fileStateViewModel = viewModel<PdfFileStateViewModel>{
        PdfFileStateViewModel(
            url = url,
            retriever = fileRetriever ?: DefaultFileRetriever(context)
        )
    }
    val fileState by fileStateViewModel.file.collectAsStateWithLifecycle()
    val downloadState by fileStateViewModel.downloadState.collectAsStateWithLifecycle()

    when (downloadState) {
        DownloadFileState.Failed -> {
            Box(
                modifier = modifier
                    .background(options.backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                fetchFailedContent?.invoke(this)
            }
        }
        DownloadFileState.Loading,
        DownloadFileState.Success -> {
            PdfViewer(
                modifier = modifier,
                file = fileState,
                loadingContent = loadingContent,
                options = options,
                pageDivider = pageDivider,
                onClick = onClick,
                onLongClick = onLongClick
            )
        }
    }
}

@Composable
fun PdfViewer(
    modifier: Modifier = Modifier,
    file: File?,
    loadingContent: @Composable BoxScope.() -> Unit = {},
    options: PdfOptions = PdfOptions(removeFileWhenFinished = false),
    pageDivider: @Composable (BoxScope.() -> Unit)? = {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline
        )
    },
    onClick: ((Offset) -> Unit)? = null,
    onLongClick: ((Offset) -> Unit)? = null,
) {
    file?.let {
        val context = LocalContext.current
        val viewModel = viewModel<PdfViewerViewModel> {
            PdfViewerViewModel(
                context = context,
                file = file,
                options = options,
            )
        }

        val initFinished by viewModel.initFinished.collectAsStateWithLifecycle()

        var zoomState = rememberZoomableState(ZoomSpec(maxZoomFactor = options.maxScale))
        val lazyColumnState = rememberLazyListState()
        val pageCount by viewModel.pageCount.collectAsStateWithLifecycle()
        val currentVisibleItems = lazyColumnState.currentVisibleItems()

        LaunchedEffect(
            key1 = currentVisibleItems + initFinished,
            block = {
                if (initFinished) {
                    viewModel.generatePagesForVisibleItems(currentVisibleItems)
                }
            }
        )
        Box(
            modifier = modifier
                .background(options.backgroundColor),
            contentAlignment = Alignment.Center
        ) {
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
                verticalArrangement = if (pageDivider == null)
                    // no divider so add all spacing between the pages
                    Arrangement.spacedBy(options.spacingBetweenPages)
                else
                    Arrangement.Top
            ) {
                items(pageCount) { pageIndex ->
                    PdfPageComposable(
                        modifier = Modifier,
                        bitmap = viewModel.getPage(pageIndex),
                        pageIndex = pageIndex
                    )

                    // half of the spacing on top and bottom of the divider
                    pageDivider?.let { divider ->
                        Box(
                            modifier = Modifier
                                .padding(vertical = options.spacingBetweenPages / 2f)
                                .fillMaxWidth()
                        ) {
                            divider()
                        }
                    }
                }
            }

            if (!initFinished) {
                loadingContent()
            }
        }

    }  ?: run {
        Box(
            modifier = modifier
                .background(options.backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            loadingContent()
        }
    }
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