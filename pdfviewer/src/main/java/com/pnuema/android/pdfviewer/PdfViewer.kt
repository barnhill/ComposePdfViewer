package com.pnuema.android.pdfviewer

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.withSign

@Composable
fun PdfViewer(
    modifier: Modifier = Modifier,
    file: File,
    maxScale: Float = 3f, //max scale 3f == 3x zoom
    allowPinchToZoom: Boolean = true,
) {
    val pdfGenerator: PdfBitmapGenerator by remember { mutableStateOf(PdfBitmapGenerator(file)) }
    var size by remember { mutableStateOf(IntSize(1, 1)) }
    var parentSize by remember { mutableStateOf(IntSize(0, 0)) }
    val state by pdfGenerator.pageCacheFlow.collectAsState(initial = pdfGenerator.cache)

    var offset by remember { mutableStateOf(Offset.Zero) }
    var scale by rememberSaveable { mutableStateOf(1f) }

    val lazyColumnState = rememberLazyListState()
    val currentVisibleItems = lazyColumnState.currentVisibleItems(pageCount = pdfGenerator.pageCount)

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
            .onGloballyPositioned {
                parentSize = it.size
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    // Wait for at least one pointer to press down
                    awaitFirstDown()
                    do {
                        val event = awaitPointerEvent()

                        when (event.changes.size) {
                            1 -> {
                                // only use x offset as y will be controlled by scrolling of the parent lazy column
                                val newOffset = (event.calculatePan() * scale)

                                // one finger gestures
                                if (scale > 1f) {
                                    offset += newOffset / scale
                                }

                                // do not consume the pointer event as vertical scrolling is handled by the lazy column and
                                // events need to reach it to scroll the list to new items
                            }

                            2 -> {
                                // two finger pinch to zoom gesture
                                if (allowPinchToZoom.not()) return@awaitEachGesture
                                val scaleInternal = event
                                    .calculateZoom()
                                    .apply {
                                        coerceIn(1f, maxScale) // Limit zoom between 100% and 500%
                                    }

                                scale = (scale * scaleInternal).coerceAtLeast(1f)

                                // keep scaling inside the bounds of the screen
                                offset = scaleInParent(
                                    parentSize = parentSize,
                                    scale = scale,
                                    offset = offset
                                )

                                //Consumes position change if there is any. This stops scrolling if there is one set to any parent Composable
                                event.changes.forEach { pointerInputChange: PointerInputChange ->
                                    pointerInputChange.consume()
                                }
                            }
                        }
                    } while(event.changes.any { it.pressed })
                }
            }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    size = coordinates.size
                }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
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

/**
 * When scaling the zoom should limit shrinking to the parents bounds, this will guarantee that
 * the pdf is always within view.
 */
private fun scaleInParent(
    parentSize: IntSize,
    scale: Float,
    offset: Offset
): Offset {
    var targetX = Float.NEGATIVE_INFINITY
    val scaledWidth = parentSize.width * scale
    val borderReached = scaledWidth - parentSize.width - 2 * abs(offset.x)
    if (borderReached < 0) {
        targetX = ((scaledWidth - parentSize.width) / 2f).withSign(offset.x)
    }

    var targetY = Float.NEGATIVE_INFINITY
    val scaledHeight = parentSize.height * scale
    val borderHeightReached = scaledHeight - parentSize.height - 2 * abs(offset.y)
    if (borderHeightReached < 0) {
        targetY = ((scaledHeight - parentSize.height) / 2f).withSign(offset.y)
    }

    if (targetX != Float.NEGATIVE_INFINITY && targetY != Float.NEGATIVE_INFINITY) {
        // change the offset to stay inside the parent
        return offset.copy(x = targetX, y = targetY)
    }

    return offset
}