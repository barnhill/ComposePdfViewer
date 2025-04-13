package com.pnuema.android.pdfviewer

sealed interface DownloadFileState {
    object Loading : DownloadFileState
    object Success : DownloadFileState
    object Failed : DownloadFileState
}