package com.example.pdfviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pdfviewer.ui.theme.PdfViewerTheme
import com.pnuema.android.pdfviewer.PdfViewer
import com.pnuema.android.pdfviewer.rememberPdfOptions
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PdfViewerTheme {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val coroutineScope = rememberCoroutineScope()
                var showContextMenu by remember { mutableStateOf(false) }
                val pdfOptions = rememberPdfOptions()

                Scaffold { padding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        color = Color.LightGray
                    ) {

                        val haptics = LocalHapticFeedback.current
                        PdfViewer(
                            url = "https://raw.githubusercontent.com/barnhill/ComposePdfViewer/main/app/src/main/assets/sample.pdf",
                            options = pdfOptions,
                            loadingContent = {
                                Text(
                                    modifier = Modifier.align(Alignment.Center),
                                    text = "Loading..."
                                )
                            },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                showContextMenu = !sheetState.isVisible
                            }
                        )
                        /*PdfViewer(
                            file = FileUtil.getTestFile(this),
                            options = pdfOptions,
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                showContextMenu = !sheetState.isVisible
                            }
                        )*/
                    }

                    if (showContextMenu) {
                        PdfOptionsMenu(
                            sheetState = sheetState,
                            onDismissRequest = { showContextMenu = false },
                            onPrintClicked = {
                                coroutineScope.launch {
                                    sheetState.hide()
                                    pdfOptions.print()
                                }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showContextMenu = false
                                    }
                                }
                            },
                            onShareClicked = {
                                coroutineScope.launch {
                                    sheetState.hide()
                                    pdfOptions.share()
                                }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showContextMenu = false
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun PdfOptionsMenu(
        modifier: Modifier = Modifier,
        sheetState: SheetState,
        onDismissRequest: () -> Unit,
        onPrintClicked: () -> Unit,
        onShareClicked: () -> Unit,
    ) {
        ModalBottomSheet(
            modifier = modifier,
            sheetState = sheetState,
            onDismissRequest = onDismissRequest
        ) {
            OutlinedButton(
                modifier = Modifier
                    .heightIn(48.dp)
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                onClick = onPrintClicked
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = stringResource(R.string.print_button_label)
                )
            }

            OutlinedButton(
                modifier = Modifier
                    .heightIn(48.dp)
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 16.dp)
                    .fillMaxWidth(),
                onClick = onShareClicked
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = stringResource(R.string.share_button_label)
                )
            }
        }
    }
}