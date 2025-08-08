# ComposePdfViewer [![Android CI](https://github.com/barnhill/ComposePdfViewer/actions/workflows/android.yml/badge.svg)](https://github.com/barnhill/ComposePdfViewer/actions/workflows/android.yml) [![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)

Android Composable for Viewing PDFs

This library provides an easy to consume Android Composable that will display PDFs.  It caches pages generated from the renderer to prevent re-rendering of the pages for the adjacent pages in the PDF to facilitate performant scrolling pages.  A default optional retriever for web hosted PDFs is provided and uses OKHttp and is compatible with HTTP 1.1, 2.

## Add the dependency

```Gradle
implementation 'com.pnuema.android:pdfviewer:1.4.1'
```
```Kotlin(KTS)
implementation("com.pnuema.android:pdfviewer:1.4.1")
```
```TOML
pdfviewer = { module = "com.pnuema.android:pdfviewer", version.ref = "1.4.1" }
```

## Usage

There are two different ways to utilize the composables.  You can either pull a pdf from a url or from a file.  Below are listed the ways to utilize from each source type.

### From a File

```kotlin
PdfViewer(file = file)
```

```file: File```
This is the file which contains the pdf to be displayed.

### From a URL

```kotlin
PdfViewer(
    url = "https://example.com/sample.pdf",
    loadingContent = {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "Loading..."
        )
    }
)
```
```url: String```
This is the url from where the pdf will be loaded from.

### Optional Parameters

#### PdfViewer - (From Url)

```fileRetriever: PDFFileRetriever```
Allow for custom retrievers to be specified and defined to allow custom retrieval of the pdf file to a target location.  Defaults to the default retriever which utilizes a basic OkHttp client with Cronet for HTTP/3 and QUIC support.

```loadingContent: @Composable BoxScope.()```
This allows for specifying a composable to display during the loading of the pdf from a url.

```fetchFailedContent: @Composable BoxScope.()```
This allows for specifying a composable to display if the retrieval of the pdf from the url fails.

```options: PdfOptions```
Configurable options for controlling how the pdf is viewed, and what actions are available

```pageDivider: @Composable```
Composable that will be displayed between each rendered page of the pdf.

```onClick: ((Offset) -> Unit)?```
Call back for single clicking on the pdf

```onLongClick: ((Offset) -> Unit)?```
Callback for long clicking on the pdf

### PdfOptions Parameters
```maxScale: Float```
Max zoom scaling factor. Defaults to 5 (500%)

```allowPinchToZoom: Boolean```
Allow the user to pinch to zoom. Defaults to true.

```backgroundColor: Color```
Background color to display behind the rendered pdf. Defaults to `Color.White`

```allowPrinting: Boolean```
Enables the printing functionality that is accessible through `options.print()`. Default: true

```allowSharing: Boolean```
Enables the sharing functionality that is accessible through `options.share()`. Default: true

```removeFileWhenFinished: Boolean```
Removes the downloaded file after the composable falls out of scope or the file passed into the 
composable if this is set to true. Default: true

### Example

There is an example application included in this repo in the app folder.  This will highlight a simple usage of both of the ways to load and display a pdf.
