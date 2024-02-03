package com.example.pdfviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.pdfviewer.ui.theme.PdfViewerTheme
import com.pnuema.android.pdfviewer.DefaultFileRetriever
import com.pnuema.android.pdfviewer.PdfViewerFromUrl
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PdfViewerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.LightGray
                ) {
                    PdfViewerFromUrl(
                        url = "https://raw.githubusercontent.com/barnhill/ComposePdfViewer/main/app/src/main/assets/sample.pdf",
                        fileRetriever = DefaultFileRetriever(targetTempFile = File(LocalContext.current.cacheDir, "temp.pdf")),
                        loadingContent = {
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                text = "Loading..."
                            )
                        }
                    )
                    /*PdfViewerFromFile(file = FileUtil.getTestFile(this))*/
                }
            }
        }
    }
}