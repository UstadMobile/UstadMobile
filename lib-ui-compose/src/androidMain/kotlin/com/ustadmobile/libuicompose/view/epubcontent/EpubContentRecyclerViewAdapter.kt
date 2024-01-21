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
): ListAdapter<String, EpubContentViewHolder>(URL_DIFFUTIL) {

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