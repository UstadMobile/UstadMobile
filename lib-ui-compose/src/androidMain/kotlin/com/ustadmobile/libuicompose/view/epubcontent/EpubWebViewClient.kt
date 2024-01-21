package com.ustadmobile.libuicompose.view.epubcontent

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import com.ustadmobile.core.domain.contententry.server.ContentEntryVersionServerUseCase
import com.ustadmobile.core.domain.contententry.server.ContentEntryVersionServerWebClient
import com.ustadmobile.core.util.xmlfilter.EpubXmlSerializerFilter
import com.ustadmobile.core.util.xmlfilter.serializeTo
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * WebViewClient used to handle epubs.
 *
 * When page loading finishes, the height will be updated to match the real content height
 *
 * It provides a flow of the loading state which can be observed for purposes of deciding
 * when to action scrolls.
 *
 * It filters XHTML to handle 'bad' content that does not specify a meta viewport tag and adds basic
 * responsiveness to it.
 */
class EpubWebViewClient(
    useCase: ContentEntryVersionServerUseCase,
    contentEntryVersionUid: Long,
    private val xmlPullParserFactory: XmlPullParserFactory,
): ContentEntryVersionServerWebClient(
    useCase = useCase,
    contentEntryVersionUid = contentEntryVersionUid,
) {

    private val _loaded = MutableStateFlow<Boolean>(false)

    val loaded: Flow<Boolean> = _loaded.asStateFlow()

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest
    ): WebResourceResponse {
        val response = super.shouldInterceptRequest(view, request)
        return if(response.mimeType == "application/xhtml+xml") {
            try {
                val xmlPullParser = xmlPullParserFactory.newPullParser()
                val serializer = xmlPullParserFactory.newSerializer()
                val bytesOut = ByteArrayOutputStream()
                serializer.setOutput(bytesOut, "UTF-8")
                xmlPullParser.setInput(response.data, "UTF-8")
                xmlPullParser.serializeTo(
                    xmlSerializer = serializer,
                    filter = EpubXmlSerializerFilter()
                )
                bytesOut.flush()
                val bytesSerialized = bytesOut.toByteArray()
                val dataIn = ByteArrayInputStream(bytesSerialized)
                val newHeaders = buildMap {
                    putAll(
                        response.responseHeaders.filter {
                            !it.key.equals("content-length", true)
                        }
                    )
                    put("content-length", bytesSerialized.size.toString())
                }

                WebResourceResponse(
                    response.mimeType, "UTF-8", response.statusCode, response.reasonPhrase,
                    newHeaders, dataIn
                )
            }catch(e: Throwable) {
                Napier.w("ERROR attempting to filter XHTML EPUB")
                response
            }
        }else {
            response
        }
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        _loaded.value = false
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        view?.adjustHeightToWrapContent()
        _loaded.value = true
    }
}