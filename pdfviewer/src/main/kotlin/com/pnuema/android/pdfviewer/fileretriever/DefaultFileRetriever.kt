package com.pnuema.android.pdfviewer.fileretriever

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.brotli.BrotliInterceptor
import okhttp3.logging.HttpLoggingInterceptor
import okio.buffer
import okio.sink
import okio.source
import java.io.File

class DefaultFileRetriever(context: Context): PDFFileRetriever {
    private var targetTempFile: File = File(context.cacheDir, "temp.pdf")

    private val client: OkHttpClient by lazy {
        OkHttpClient
            .Builder()
            .cache(
                Cache(
                    directory = File(context.cacheDir, "pdfviewer_http_cache"),
                    maxSize = 10L * 1024L * 1024L // 10 MiB
                )
            )
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.HEADERS
                }
            )
            .addInterceptor(BrotliInterceptor)
            .build()
    }

    override suspend fun from(source: String): File? {
        val request: Request = Request.Builder().url(source).build()
        return client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                response.body?.byteStream()?.let { body ->
                    body.source().use { input ->
                        targetTempFile.sink().use {
                            it.buffer().use { sink ->
                                sink.writeAll(input)
                                sink.flush()
                            }
                        }
                    }
                    targetTempFile
                }
            } else {
                null
            }
        }
    }
}