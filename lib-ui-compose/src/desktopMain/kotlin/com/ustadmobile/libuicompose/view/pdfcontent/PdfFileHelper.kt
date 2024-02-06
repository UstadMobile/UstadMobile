package com.ustadmobile.libuicompose.view.pdfcontent

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.rendering.PDFRenderer
import java.io.File

/**
 * PDFBox helper to
 *
 * 1) Ensure that all access to the PDFBox object is done through a single (but not Main) thread
 * 2) Close documents when finished.
 */
class PdfFileHelper {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val context = newSingleThreadContext("pdf-context")

    private val scope = CoroutineScope(context + Job())

    private val _numPages = MutableStateFlow(0)

    val numPages: Flow<Int> = _numPages.asStateFlow()

    private val _doc = MutableStateFlow<PdfFileHelperState?>(null)

    class PdfFileHelperState(
        val doc: PDDocument,
        val renderer: PDFRenderer,
    )

    fun load(file: File) {
        scope.launch {
            val loadedDoc = Loader.loadPDF(file)
            val renderer = PDFRenderer(loadedDoc)
            val oldState = _doc.getAndUpdate {
                PdfFileHelperState(loadedDoc, renderer)
            }
            oldState?.doc?.close()

            _numPages.value = loadedDoc.numberOfPages
        }
    }


    //https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Image_And_Icons_Manipulations/README.md
    suspend fun getSize(page: Int): PDRectangle? {
        return withContext(context) {
            _doc.value?.doc?.getPage(page)?.cropBox
        }
    }

    suspend fun loadPage(page: Int, scale: Float) : ImageBitmap? {
        return withContext(context) {
            _doc.value?.renderer?.renderImage(page, scale)?.toComposeImageBitmap()
        }
    }

    fun close() {
        scope.cancel()
        context.close()
        _doc.value?.doc?.close()
    }


}