package com.ustadmobile.libuicompose.view.epubcontent

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ustadmobile.core.domain.contententry.server.ContentEntryVersionServerUseCase
import com.ustadmobile.core.util.ext.dpAsPx
import com.ustadmobile.core.viewmodel.epubcontent.EpubScrollCommand
import com.ustadmobile.libuicompose.R
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * @param scrollCommandFlow The flow of scroll commands. This will be observed by each ViewHolder
 *        so that it can action scroll commands as required a) requesting focus - see note and b)
 *        scrolling to a hash link within the content.
 */
class EpubContentRecyclerViewAdapter(
    private val contentEntryVersionServer: ContentEntryVersionServerUseCase,
    private val contentEntryVersionUid: Long,
    private val scrollCommandFlow: Flow<EpubScrollCommand>,
): ListAdapter<String, EpubContentRecyclerViewAdapter.EpubContentViewHolder>(URL_DIFFUTIL) {


    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    private var mRecyclerView: RecyclerView? = null

    /**
     * Javascript interface that is used as part of the system to manage scrolling to a hash link
     * e.g. #anchor
     */
    inner class ScrollDownJavascriptInterface {

        @Suppress("unused")
        @JavascriptInterface
        @Keep
        fun scrollDown(amount: Float) {
            Napier.d { "EpubContent: scrollDown callback: $amount dp"}
            mRecyclerView?.post {
                mRecyclerView?.scrollBy(0, amount.dpAsPx)
            }
        }

    }

    private val mScrollDownInterface = ScrollDownJavascriptInterface()

    inner class EpubContentViewHolder(
        val webView: WebView,
        private val _pageIndex: MutableStateFlow<Int>,
        private val _loadedState: Flow<Boolean>,
    ) : RecyclerView.ViewHolder(webView) {

        internal var pageIndex: Int
            get() = _pageIndex.value
            set(value) {
                _pageIndex.value = value
            }

        init {
            coroutineScope.launch {
                /**
                 * Move the focus to the given child. This will ensure that the primary page (e.g. the
                 * first visible item that takes up at least half the screen) will stay in place when other
                 * views move around as WebViews load and heights are changed.
                 *
                 * Otherwise the first (even slightly) visible item will be given focus. When scrolling up,
                 * the previous page would load, get taller, and then suddenly what the user was reading
                 * would no longer be visible.
                 */
                scrollCommandFlow.combine(_pageIndex) { scrollCmd, pageIndex ->
                    scrollCmd to pageIndex
                }.collectLatest { commandAndIndex ->
                    val (scrollCmd, pageIndex) = commandAndIndex
                    Napier.d { "EpubContent: cmd index=${scrollCmd.spineIndex} this index=${pageIndex}" }

                    if(scrollCmd.spineIndex == pageIndex) {
                        //pick this up, its for us
                        Napier.d { "EpubContent: Requesting focus on index $pageIndex" }
                        webView.requestFocus()

                        /*
                         * If there is a hash anchor that we need to reach within the webview...
                         */
                        val scrollToHash = scrollCmd.hash
                        if(scrollToHash != null) {
                            Napier.d { "EpubContent: scroll to hash $scrollToHash" }
                            _loadedState.filter { it }.first()
                            webView.scrollToAnchor(scrollToHash.removePrefix("#"))
                        }
                    }
                }
            }
        }
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        mRecyclerView = null
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpubContentViewHolder {
        val webView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_epub_contentview, parent, false
        ) as WebView

        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
        webView.addJavascriptInterface(mScrollDownInterface, SCROLL_DOWN_JAVASCRIPT_INTERFACE_NAME)

        val pageIndexFlow = MutableStateFlow(-1)
        val webViewClient = EpubWebViewClient(
            useCase = contentEntryVersionServer,
            contentEntryVersionUid = contentEntryVersionUid,
        )
        webView.webViewClient = webViewClient

        return EpubContentViewHolder(webView, pageIndexFlow, webViewClient.loaded)
    }

    override fun onBindViewHolder(holder: EpubContentViewHolder, position: Int) {
        holder.webView.loadUrl(getItem(position))
        holder.pageIndex = position
    }

    override fun onViewRecycled(holder: EpubContentViewHolder) {
        super.onViewRecycled(holder)

        holder.webView.adjustHeightToDisplayHeight()
        holder.webView.loadUrl("about:blank")
    }

    companion object {

        const val SCROLL_DOWN_JAVASCRIPT_INTERFACE_NAME = "UstadEpub"


        private val URL_DIFFUTIL = object:  DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
        }

    }

}