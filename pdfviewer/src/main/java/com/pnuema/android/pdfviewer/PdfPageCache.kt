package com.pnuema.android.pdfviewer

import android.graphics.Bitmap
import android.util.Log
import android.util.LruCache

class PdfPageCache {
    val cacheSize = (0.05 * 1024 * 1024).toInt() // 50k cache

    private val memoryCache = object : LruCache<Int, Bitmap>(cacheSize) {

        override fun sizeOf(key: Int, bitmap: Bitmap): Int {
            // The cache size will be measured in kilobytes rather than
            // number of items.
            return bitmap.byteCount / 1024
        }
    }

    fun put(page: Int, bitmap: Bitmap) {
        Log.d("PdfViewer", "Inserting page $page into cache")
        memoryCache.put(page, bitmap)
    }

    fun get(page: Int): Bitmap? = memoryCache[page] ?: run {
        null
    }

    fun contains(page: Int) = memoryCache[page] != null
}