# ComposePdfViewer [![Android CI](https://github.com/barnhill/ComposePdfViewer/actions/workflows/android.yml/badge.svg)](https://github.com/barnhill/ComposePdfViewer/actions/workflows/android.yml) [![API](https://img.shields.io/badge/API-19%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=23)

Android Composable for Viewing PDFs

The goal of this library is to create and provide an easy to consume Android Composable that will display PDFS.  It will also cache pages generated from the PdfRenderer to prevent re-rendering of the pages for the adjacent pages in the PDF to help performance of scrolling pages.


## Usage
Add the dependency:

```Gradle
implementation 'com.pnuema.android:pdfviewer:1.0.2'
```
```Kotlin(KTS)
implementation("com.pnuema.android:pdfviewer:1.0.2")
```

## To Use:

Retrieve the pdf file from the location it is stored either from a url or from the file system.  Get a reference to the File that points to this file on disk.
```
val file = getPdfFile() //retrieve pdf file from source (url or file system...etc)
```

In the Composable that you intend to show the pdf in use the following syntax to utilize the PdfViewer Composable provided by this library to display the PDF.
```
PdfViewer(file = file)
```
