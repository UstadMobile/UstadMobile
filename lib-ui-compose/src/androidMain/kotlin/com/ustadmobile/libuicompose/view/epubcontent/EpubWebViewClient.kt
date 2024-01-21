package com.ustadmobile.libuicompose.view.epubcontent

import android.graphics.Bitmap
import android.webkit.WebView
import com.ustadmobile.core.domain.contententry.server.ContentEntryVersionServerUseCase
import com.ustadmobile.core.domain.contententry.server.ContentEntryVersionServerWebClient

/**
 * WebViewClient used to handle epubs.
 *
 * When page loading finishes, the height will be updated to match the real content height
 */
class EpubWebViewClient(
    useCase: ContentEntryVersionServerUseCase,
    contentEntryVersionUid: Long,
): ContentEntryVersionServerWebClient(
    useCase = useCase,
    contentEntryVersionUid = contentEntryVersionUid,
) {

    var loaded: Boolean = false

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        loaded = false
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        loaded = true
        view?.adjustHeightToWrapContent()
    }
}