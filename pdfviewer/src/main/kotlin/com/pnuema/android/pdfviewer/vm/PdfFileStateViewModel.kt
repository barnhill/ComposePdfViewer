package com.pnuema.android.pdfviewer.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pnuema.android.pdfviewer.fileretriever.PDFFileRetriever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class PdfFileStateViewModel(
    val url: String,
    val retriever: PDFFileRetriever
): ViewModel() {
    private val _file = MutableStateFlow<File?>(null)
    val file = _file.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _file.emit(retriever.from(url))
        }
    }

    override fun onCleared() {
        _file.value?.delete()
        super.onCleared()
    }
}