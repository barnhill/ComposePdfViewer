package com.pnuema.android.pdfviewer

import java.io.File

interface PDFFileRetriever {
    fun from(source: String): File?
}