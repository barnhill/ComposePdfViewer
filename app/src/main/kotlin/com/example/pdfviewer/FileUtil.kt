package com.example.pdfviewer

import android.content.Context
import java.io.File
import java.io.FileOutputStream

object FileUtil {
    fun getTestFile(context: Context): File = File(context.cacheDir, "temp.pdf").apply {
        context.assets.open("sample.pdf").use { input ->
            FileOutputStream(this).use { output ->
                output.write(input.readBytes())
            }
        }
    }
}