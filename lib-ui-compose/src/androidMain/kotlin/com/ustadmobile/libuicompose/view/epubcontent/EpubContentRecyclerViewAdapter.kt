package com.ustadmobile.libuicompose.view.epubcontent

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.ustadmobile.core.domain.contententry.server.ContentEntryVersionServerUseCase
import com.ustadmobile.libuicompose.R

class EpubContentRecyclerViewAdapter(
    private val contentEntryVersionServer: ContentEntryVersionServerUseCase,
    private val contentEntryVersionUid: Long,
    private val getDecorHeight: () -> Int,
): ListAdapter<String, EpubContentViewHolder>(URL_DIFFUTIL) {

    private val boundHolders = mutableListOf<EpubContentViewHolder>()

    private var nextFocus: Int = -1

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpubContentViewHolder {
        val webView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_epub_contentview, parent, false
        ) as WebView

        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
        webView.webViewClient = EpubWebViewClient(
            useCase = contentEntryVersionServer,
            contentEntryVersionUid = contentEntryVersionUid,
        )

        return EpubContentViewHolder(webView)
    }

    override fun onBindViewHolder(holder: EpubContentViewHolder, position: Int) {
        holder.webView.loadUrl(getItem(position))
        holder.pageIndex = position

        if(nextFocus == position) {
            holder.webView.requestFocus()
            nextFocus = -1
        }
    }

    override fun onViewRecycled(holder: EpubContentViewHolder) {
        super.onViewRecycled(holder)

        holder.webView.adjustHeightToDisplayHeight()
        holder.webView.loadUrl("about:blank")
        boundHolders -= holder
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
    fun focusChildPosition(position: Int) {
        val boundHolder = boundHolders.filter {
            it.pageIndex == position
        }.firstOrNull()

        if(boundHolder != null) {
            val posOnWindow = IntArray(2).apply {
                boundHolder.webView.getLocationInWindow(this)
            }

            if(posOnWindow[1] + boundHolder.webView.height < (getDecorHeight() / 2)) {
                focusChildPosition(position + 1)
            }else {
                boundHolder.webView.requestFocus()
                nextFocus = -1
            }
        }else {
            nextFocus = position
        }
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