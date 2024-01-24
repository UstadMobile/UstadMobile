package com.ustadmobile.core.domain.contententry.server

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import com.ustadmobile.core.webview.UstadAbstractWebViewClient
import io.github.aakira.napier.Napier
import net.thauvin.erik.urlencoder.UrlEncoderUtil

/**
 * Implements an Android WebViewClient to intercept resource requests for a content item. It will
 * translate the url requested e.g. http://endpoint:port/api/content/versionUid/path/in/content
 * to the correct response (e.g. using bodyDataUrl for the body source and headers as
 * specified on the ContentManifestEntry) using ContentEntryVersionServerUseCase.
 *
 * ContentEntryVersionServerUseCase itself uses OkHttpClient, which uses the cache, so if the item
 * was downloaded for offline use, it will work.
 */
open class ContentEntryVersionServerWebClient(
    private val useCase: ContentEntryVersionServerUseCase,
    private val contentEntryVersionUid: Long,
): UstadAbstractWebViewClient() {


    /**
     * Seems like this does not work for video as per:
     *  https://github.com/ionic-team/capacitor/issues/6021
     *
     *  Seems that preloading causes the problem. Thank you, Google.
     */
    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest
    ): WebResourceResponse {
        val url = request.url.toString()
        return try {
            val pathInContentEntryVersion = url.substringAfter(
                "/api/content/$contentEntryVersionUid/")
            val okHttpResponse = useCase(
                request = request.toCacheRequest(),
                contentEntryVersionUid = contentEntryVersionUid,
                pathInContentEntryVersion = UrlEncoderUtil.decode(pathInContentEntryVersion),
            )
            Napier.d { "ContentEntryVersionServerWebClient: ${okHttpResponse.code} " +
                    "${okHttpResponse.message} $url " }

            okHttpResponse.asWebResourceResponse()
        }catch(e: Throwable) {
            Napier.w("ContentEntryVersionServerWebClient: could not serve $url", e)
            newUnavailableWebResponse(e)
        }
    }
}