package com.pnuema.android.pdfviewer.fileretriever

import java.io.File

interface PDFFileRetriever {
    suspend fun from(source: String): File?
}