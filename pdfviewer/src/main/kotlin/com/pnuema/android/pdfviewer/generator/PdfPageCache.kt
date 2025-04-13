package com.pnuema.android.pdfviewer.generator

import android.graphics.Bitmap
import android.util.LruCache

internal class PdfPageCache() {
    private val maxCacheSize = 50 * 1024 //50k cache size
    private val memoryCache = object : LruCache<Int, Bitmap>(maxCacheSize) {
        override fun sizeOf(
            key: Int, bitmap: Bitmap
        ): Int {
            // The cache size will be measured in kilobytes rather than
            // number of items.
            return bitmap.byteCount / 1024
        }
    }

    fun put(page: Int, bitmap: Bitmap) {
        synchronized(memoryCache) {
            memoryCache.put(page, bitmap)
        }
    }

    fun get(page: Int): Bitmap? = memoryCache[page] ?: run {
        null
    }

    fun contains(page: Int) = memoryCache[page] != null
}