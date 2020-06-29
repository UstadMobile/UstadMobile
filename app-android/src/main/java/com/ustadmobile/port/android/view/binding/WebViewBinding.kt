package com.ustadmobile.port.android.view.binding

import android.webkit.WebView
import androidx.databinding.BindingAdapter

@BindingAdapter("webUrl")
fun WebView.loadWebUrl(url: String?) {
    if(url != null){
        this.loadUrl(url)
    }
}
