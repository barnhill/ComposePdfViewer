package com.pnuema.android.pdfviewer.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pnuema.android.pdfviewer.DownloadFileState
import com.pnuema.android.pdfviewer.fileretriever.PDFFileRetriever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class PdfFileStateViewModel(
    val url: String,
    val retriever: PDFFileRetriever
): ViewModel() {
    private val _file = MutableStateFlow<File?>(null)
    val file = _file.asStateFlow()

    private val _downloadState = MutableStateFlow<DownloadFileState>(DownloadFileState.Loading)
    val downloadState = _downloadState.asStateFlow()

    init {
        _downloadState.update { DownloadFileState.Loading }
        viewModelScope.launch(Dispatchers.IO) {
            val result = retriever.from(url)
            result?.let { r ->
                _file.update { r }
                _downloadState.update { DownloadFileState.Success }
            } ?: run {
                _file.update { null }
                _downloadState.update { DownloadFileState.Failed }
            }
        }
    }

    override fun onCleared() {
        _file.value?.delete()
        super.onCleared()
    }
}