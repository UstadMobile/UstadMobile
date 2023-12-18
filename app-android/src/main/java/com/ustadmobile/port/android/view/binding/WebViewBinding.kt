package com.ustadmobile.port.android.view.binding

import android.webkit.WebView
import androidx.databinding.BindingAdapter

@BindingAdapter("htmlData")
fun WebView.loadHtmlData(htmlData: String?) {
    loadData(htmlData ?: "<html></html>", "text/html", "UTF-8")
}
