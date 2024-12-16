package com.pnuema.android.pdfviewer

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.util.LruCache

class PdfPageCache(context: Context, private val percent: Double = 0.15) {

    private val maxCacheSize = calculateMaxCacheSize(context)
    private val memoryCache = object : LruCache<Int, Bitmap>(maxCacheSize) {
        override fun sizeOf(
            key: Int, bitmap: Bitmap
        ): Int {
            // The cache size will be measured in kilobytes rather than
            // number of items.
            return bitmap.byteCount / 1024
        }
    }

    private fun calculateMaxCacheSize(context: Context): Int {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val availableMemory = memoryInfo.availMem
        return (availableMemory * percent).toInt()
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