package com.ustadmobile.libuicompose.view.epubcontent

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ustadmobile.core.domain.contententry.server.ContentEntryVersionServerUseCase
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

class EpubContentRecyclerViewAdapter(
    private val contentEntryVersionServer: ContentEntryVersionServerUseCase,
    private val contentEntryVersionUid: Long,
    private val getDecorHeight: () -> Int,
): ListAdapter<String, EpubContentRecyclerViewAdapter.EpubContentViewHolder>(URL_DIFFUTIL) {


    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    private val currentScrollCommand = MutableStateFlow<EpubScrollCommand?>(null)

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
                currentScrollCommand.combine(_pageIndex) { scrollCmd, pageIndex ->
                    scrollCmd to pageIndex
                }.collectLatest { commandAndIndex ->
                    val (scrollCmd, pageIndex) = commandAndIndex
                    if(scrollCmd?.spineIndex == pageIndex) {
                        //pick this up, its for us
                        Napier.d { "Requesting focus on index $pageIndex" }
                        webView.requestFocus()

                        /*
                         * If there is a hash anchor that we need to reach within the webview...
                         */
                        val scrollToHash = scrollCmd.hash
                        if(scrollToHash != null) {
                            _loadedState.filter { it }.first()
                            webView.scrollToAnchor(scrollToHash)
                        }
                    }
                }
            }
        }

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


    /**
     * Move the focus to the given child. This will ensure that the primary page (e.g. the
     * first visible item that takes up at least half the screen) will stay in place when other
     * views move around as WebViews load and heights are changed.
     *
     * Otherwise the first (even slightly) visible item will be given focus. When scrolling up,
     * the previous page would load, get taller, and then suddenly what the user was reading
     * would no longer be visible.
     */
    fun onScrollCommand(
        scrollCommand: EpubScrollCommand
    ) {
        currentScrollCommand.value = scrollCommand
    }


    companion object {

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