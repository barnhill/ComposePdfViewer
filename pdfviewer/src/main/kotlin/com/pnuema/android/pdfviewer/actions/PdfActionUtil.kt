package com.pnuema.android.pdfviewer.actions

import android.content.Context
import android.content.Context.PRINT_SERVICE
import android.content.Intent
import android.print.PrintManager
import androidx.compose.ui.unit.IntSize
import androidx.core.content.FileProvider
import com.pnuema.android.pdfviewer.generator.PdfBitmapGenerator
import com.pnuema.android.pdfviewer.print.PdfPrintDocumentAdapter
import java.io.File


object PdfActionUtil {
    fun print(
        context: Context,
        pdfGenerator: PdfBitmapGenerator,
        size: IntSize
    ) {
        val printManager = context.getSystemService(PRINT_SERVICE) as PrintManager
        val jobName = "Document"

        printManager.print(
            jobName,
            PdfPrintDocumentAdapter(
                context = context,
                pdfGenerator = pdfGenerator,
                pageSize = size
            ),
            null
        )
    }

    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".provider", file
        )
        context.startActivity(
            Intent(Intent.ACTION_SEND).apply {
                setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setType("application/pdf")
                putExtra(Intent.EXTRA_STREAM, uri)
            }
        )
    }
}