# ComposePdfViewer [![Android CI](https://github.com/barnhill/ComposePdfViewer/actions/workflows/android.yml/badge.svg)](https://github.com/barnhill/ComposePdfViewer/actions/workflows/android.yml) [![API](https://img.shields.io/badge/API-19%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=23)

Android Composable for Viewing PDFs

This library provides an easy to consume Android Composable that will display PDFs.  It caches pages generated from the renderer to prevent re-rendering of the pages for the adjacent pages in the PDF to facilitate performant scrolling pages.  A default optional retriever for web hosted PDFs is provided and is compatible with HTTP 1.1, 2, and 3 (QUIC).

## Add the dependency

```Gradle
implementation 'com.pnuema.android:pdfviewer:1.2.1'
```
```Kotlin(KTS)
implementation("com.pnuema.android:pdfviewer:1.2.1")
```
```TOML
pdfviewer = { module = "com.pnuema.android:pdfviewer", version.ref = "1.2.1" }
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

### Common Parameters
```maxScale: Float```
Max zoom scaling factor. Defaults to 5 (500%)

```allowPinchToZoom: Boolean```
Allow the user to pinch to zoom. Defaults to true.

```backgroundColor: Color```
Background color to display behind the rendered pdf. Defaults to `Color.White`

```pageDivider: @Composable```
Composable that will be displayed between each rendered page of the pdf.

### Example

There is an example application included in this repo in the app folder.  This will highlight a simple usage of both of the ways to load and display a pdf.
