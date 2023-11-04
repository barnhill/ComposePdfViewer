package com.pnuema.android.pdfviewer

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import java.io.File
import kotlin.math.max
import kotlin.math.min

@Composable
fun PdfViewer(
    modifier: Modifier = Modifier,
    file: File,
    maxScale: Float = 5f, //max scale 5f == 5x zoom
    allowPinchToZoom: Boolean = true,
) {
    val pdfGenerator: PdfBitmapGenerator by remember { mutableStateOf(PdfBitmapGenerator(file)) }
    var size by remember { mutableStateOf(IntSize(1, 1)) }
    val state by pdfGenerator.pageCacheFlow.collectAsState(initial = pdfGenerator.cache)

    val lazyColumnState = rememberLazyListState()
    val currentVisibleItems = lazyColumnState.currentVisibleItems(pageCount = pdfGenerator.pageCount)
    val zoomState = rememberZoomableState(
        zoomSpec = ZoomSpec(maxZoomFactor = maxScale)
    )

    LaunchedEffect(
        key1 = currentVisibleItems,
        block = {
            val lowerPageBound = max(0, currentVisibleItems.first() - 2) // 2 pages prior to first visible or 0
            val upperPageBound = min(pdfGenerator.pageCount - 1, currentVisibleItems.last() + 2) // 2 pages after last visible or max page

            for (i in lowerPageBound..upperPageBound) {
                pdfGenerator.getPdfBitmap(i, size)
            }
        }
    )

    Box(
        modifier
            .fillMaxSize()
            .background(Color.White)
            .zoomable(
                state = zoomState,
                enabled = allowPinchToZoom,
            ),
    ) {
        LazyColumn(
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    size = coordinates.size
                }
                .fillMaxSize(),
            state = lazyColumnState,
            content = {
                items(pdfGenerator.pageCount) { pageIndex ->
                    if (size.width == 1) return@items

                    PdfPageComposable(
                        bitmap = state.get(pageIndex.toString()),
                        pageIndex = pageIndex
                    )

                    Divider(
                        modifier = Modifier.padding(16.dp),
                        color = Color.Transparent
                    )
                }
            }
        )
    }
}

@Composable
private fun LazyListState.currentVisibleItems(pageCount: Int): List<Int> {
    return remember(this) {
        derivedStateOf {
            when (layoutInfo.visibleItemsInfo.count()) {
                pageCount -> listOf(0)
                0 -> listOf(0)
                else -> {
                    layoutInfo.visibleItemsInfo.map { it.index }
                }
            }
        }
    }.value
}

@Composable
private fun PdfPageComposable(
    bitmap: Bitmap?,
    pageIndex: Int
) {
    if (bitmap != null) {
        Image(
            modifier = Modifier
                .aspectRatio(1f)
                .clipToBounds()
                .fillMaxWidth(),
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Page $pageIndex"
        )
    }
}