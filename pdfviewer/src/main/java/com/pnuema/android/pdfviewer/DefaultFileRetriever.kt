package com.pnuema.android.pdfviewer

import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import okio.source
import java.io.File


class DefaultFileRetriever(
    private val okHttpClient: OkHttpClient = OkHttpClient(),
    private val targetTempFile: File,
): PDFFileRetriever, AutoCloseable {
    override fun from(source: String): File? {
        val request: Request = Request.Builder().url(source).build()
        okHttpClient.newCall(request).execute().use { response ->
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