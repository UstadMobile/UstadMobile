package com.ustadmobile.libuicompose.view.epubcontent

import android.webkit.WebView
import androidx.recyclerview.widget.RecyclerView

class EpubContentViewHolder(
    val webView: WebView,
) : RecyclerView.ViewHolder(webView) {

    internal var pageIndex: Int = 0

}


