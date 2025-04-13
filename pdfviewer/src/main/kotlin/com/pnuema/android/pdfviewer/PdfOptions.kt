package com.pnuema.android.pdfviewer
import android.os.Parcelable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import com.pnuema.android.pdfviewer.actions.PdfAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.parcelize.Parcelize

data class PdfOptions(
    val maxScale: Float = 5f,
    val allowPinchToZoom: Boolean = true,
    val allowPrinting: Boolean = true,
    val allowSharing: Boolean = true,
    val removeFileWhenFinished: Boolean = true,
    val backgroundColor: Color = Color.White,
) {
    private val _action: MutableStateFlow<PdfAction?> = MutableStateFlow(null)
    internal val action = _action.asStateFlow()

    fun print() {
        if (allowPrinting) {
            _action.update { PdfAction.Print }
        }
    }

    fun share() {
        if (allowSharing) {
            _action.update { PdfAction.Share }
        }
    }

    fun clearAction() {
        _action.update { null }
    }

    companion object {
        internal val Saver = Saver<PdfOptions, PdfOptionsSavedState>(
            save = { state ->
                PdfOptionsSavedState(
                    maxScale = state.maxScale,
                    allowPinchToZoom = state.allowPinchToZoom,
                    allowPrinting = state.allowPrinting,
                    allowSharing = state.allowSharing,
                    removeFileWhenFinished = state.removeFileWhenFinished,
                    backgroundColor = state.backgroundColor.value,
                )
            },
            restore = { saved ->
                PdfOptions(
                    maxScale = saved.maxScale,
                    allowPinchToZoom = saved.allowPinchToZoom,
                    allowPrinting = saved.allowPrinting,
                    allowSharing = saved.allowSharing,
                    removeFileWhenFinished = saved.removeFileWhenFinished,
                    backgroundColor = Color(saved.backgroundColor)
                )
            },
        )
    }
}

@Composable
fun rememberPdfOptions(
    maxScale: Float = 5f,
    allowPinchToZoom: Boolean = true,
    allowPrinting: Boolean = true,
    allowSharing: Boolean = true,
    removeFileWhenFinished: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
): PdfOptions = rememberSaveable(saver = PdfOptions.Saver) {
    PdfOptions(
        maxScale = maxScale,
        allowPinchToZoom = allowPinchToZoom,
        allowPrinting = allowPrinting,
        allowSharing = allowSharing,
        removeFileWhenFinished = removeFileWhenFinished,
        backgroundColor = backgroundColor,
    )
}

@Parcelize
data class PdfOptionsSavedState(
    val maxScale: Float,
    val allowPinchToZoom: Boolean,
    val allowPrinting: Boolean,
    val allowSharing: Boolean,
    val removeFileWhenFinished: Boolean,
    val backgroundColor: ULong,
    ) : Parcelable
