package com.pnuema.android.pdfviewer.actions

sealed interface PdfAction {
    data object Print : PdfAction
    data object Share : PdfAction
}