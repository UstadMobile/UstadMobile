package com.ustadmobile.libuicompose.view.epubcontent

import android.graphics.Bitmap
import android.webkit.WebView
import com.ustadmobile.core.domain.contententry.server.ContentEntryVersionServerUseCase
import com.ustadmobile.core.domain.contententry.server.ContentEntryVersionServerWebClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * WebViewClient used to handle epubs.
 *
 * When page loading finishes, the height will be updated to match the real content height
 *
 * It provides a flow of the loading state which can be observed for purposes of deciding
 * when to action scrolls.
 */
class EpubWebViewClient(
    useCase: ContentEntryVersionServerUseCase,
    contentEntryVersionUid: Long,
): ContentEntryVersionServerWebClient(
    useCase = useCase,
    contentEntryVersionUid = contentEntryVersionUid,
) {

    private val _loaded = MutableStateFlow<Boolean>(false)

    val loaded: Flow<Boolean> = _loaded.asStateFlow()

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        _loaded.value = false
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        view?.adjustHeightToWrapContent()
        _loaded.value = true
    }
}