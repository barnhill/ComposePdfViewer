package com.pnuema.android.pdfviewer

import android.content.Context
import androidx.collection.LruCache
import com.google.android.gms.net.CronetProviderInstaller
import com.google.net.cronet.okhttptransport.CronetInterceptor
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import okio.buffer
import okio.sink
import okio.source
import org.chromium.net.CronetEngine
import java.io.File

class DefaultFileRetriever(
    context: Context,
    okHttpClient: OkHttpClient? = null,
    private val targetTempFile: File,
): PDFFileRetriever, AutoCloseable {

    private val client: OkHttpClient

    init {
        client = okHttpClient ?: run {
            CronetProviderInstaller.installProvider(context)
            val engine = CronetEngine.Builder(context.applicationContext)
                .enableBrotli(true)
                .setStoragePath(context.cacheDir.absolutePath)
                .enableHttpCache(CronetEngine.Builder.HTTP_CACHE_DISK, 1024 * 1024 * 25) // 25mb cache
                .build()
            val cronetInterceptor = CronetInterceptor.newBuilder(engine).build()

            OkHttpClient
                .Builder()
                .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
                .addInterceptor(cronetInterceptor)
                .build()
        }
    }

    override fun from(source: String): File? {
        val request: Request = Request.Builder().url(source).build()
        client.newCall(request).execute().use { response ->
            return response.body?.byteStream()?.let { body ->
                targetTempFile.sink().buffer().use { sink ->
                    sink.writeAll(body.source())
                    sink.flush()
                }
                targetTempFile
            }
        }
    }

    override fun close() {}
}