package com.ustadmobile.libuicompose.view.pdfcontent

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.ustadmobile.door.util.systemTimeInMillis
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
import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * PDFBox helper to
 *
 * 1) Ensure that all access to the PDFBox object is done through a single (but not Main) thread
 * 2) Close documents when finished.
 *
 * When the PDF file is loaded, the size of all pages will be loaded and saved at the same time.
 * This will allow for the instant creation of correctly sized placeholder images.
 */
class PdfFileHelper(
    private val pageCacheSize: Int = 10,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val context = newSingleThreadContext("pdf-context")

    private val scope = CoroutineScope(context + Job())

    private val _numPages = MutableStateFlow(0)

    val numPages: Flow<Int> = _numPages.asStateFlow()

    private val _doc = MutableStateFlow<PdfFileHelperState?>(null)

    private data class PageCacheEntry(
        val imageBitmap: ImageBitmap,
        val lastAccessTime: Long,
    )

    private data class PageAndScale(
        val page: Int,
        val scale: Float,
    )

    private val pageCache = ConcurrentHashMap<PageAndScale, PageCacheEntry>()

    private fun getPageAndUpdateLastAccessTime(pageAndScale: PageAndScale): ImageBitmap? {
        val pageEntry = pageCache[pageAndScale]
        if(pageEntry != null) {
            pageCache[pageAndScale] = pageEntry.copy(
                lastAccessTime = systemTimeInMillis()
            )
        }

        return pageEntry?.imageBitmap
    }

    class PdfFileHelperState(
        val doc: PDDocument,
        val renderer: PDFRenderer,
        val pageSizes: List<PDRectangle>,
    )

    fun load(file: File) {
        scope.launch {
            val loadedDoc = Loader.loadPDF(file)
            val renderer = PDFRenderer(loadedDoc)
            val pageSizes = (0 until loadedDoc.numberOfPages).map {
                loadedDoc.getPage(it).cropBox
            }

            val oldState = _doc.getAndUpdate {
                PdfFileHelperState(loadedDoc, renderer, pageSizes)
            }
            oldState?.doc?.close()

            _numPages.value = loadedDoc.numberOfPages
        }
    }

    fun getPageSize(page: Int): PDRectangle? {
        return _doc.value?.pageSizes?.get(page)
    }

    suspend fun loadPage(page: Int, scale: Float) : ImageBitmap? {
        val pageAndScale = PageAndScale(page, scale)
        return getPageAndUpdateLastAccessTime(pageAndScale) ?: withContext(context) {
            _doc.value?.renderer?.renderImage(page, scale)?.toComposeImageBitmap()?.also {
                pageCache[pageAndScale] = PageCacheEntry(it, systemTimeInMillis())
                if(pageCache.size > pageCacheSize) {
                    val pageToRemove = pageCache.entries.minBy { it.value.lastAccessTime }
                    pageCache.remove(pageToRemove.key)
                }
            }
        }
    }

    /**
     * If a page has not yet been loaded, a BufferedImage can be created instantly with the correct
     * size. This avoids any jumping that would otherwise happen if sizes change.
     */
    fun getPlaceholderImage(page: Int, scale: Float) : ImageBitmap {
        return _doc.value?.pageSizes?.get(page)?.let {
            BufferedImage((it.width * scale).toInt(), (it.height * scale).toInt(), BufferedImage.TYPE_INT_ARGB)
        }?.toComposeImageBitmap() ?: throw IllegalArgumentException("No size known for page $page")
    }

    fun getCachedPage(page: Int, scale: Float) : ImageBitmap? {
        return getPageAndUpdateLastAccessTime(PageAndScale(page, scale))
    }

    fun close() {
        scope.cancel()
        context.close()
        _doc.value?.doc?.close()
    }


}