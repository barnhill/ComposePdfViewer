package com.pnuema.android.pdfviewer

import android.graphics.Bitmap
import android.util.Log
import android.util.LruCache
import androidx.core.graphics.BitmapCompat

class PdfPageCache {
    val cacheSize = (0.05 * 1024 * 1024).toInt() // 50k cache

    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {

        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            // The cache size will be measured in kilobytes rather than
            // number of items.
            return bitmap.byteCount / 1024
        }
    }

    fun put(page: String, bitmap: Bitmap) {
        Log.d("PdfViewer", "Inserting page $page into cache")
        memoryCache.put(page, bitmap)
    }

    fun get(page: String): Bitmap? = memoryCache[page] ?: run {
        null
    }

    fun contains(page: String) = memoryCache[page] != null
}