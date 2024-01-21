package com.ustadmobile.libuicompose.view.epubcontent

import android.content.res.Resources
import android.view.ViewGroup
import android.webkit.WebView


/**
 * This will set the height of the WebView to match the height of the display. This is useful when
 * we have the WebView in a RecyclerView as it will avoid the RecyclerView creating too many
 * ViewHolders at the start. This will happen if the WebView's height is set to wrap_content before
 * the page loads, as the height will be considered as 0 before it loads.
 */
@Suppress("unused")
fun WebView.adjustHeightToDisplayHeight() {
    layoutParams = layoutParams.also {
        it.height = Resources.getSystem().displayMetrics.heightPixels
    }
}

/**
 * Adjust the height of the WebView to WRAP_CONTENT (e.g. use this after content has loaded)
 */
fun WebView.adjustHeightToWrapContent() {
    layoutParams = layoutParams.also {
        it.height = ViewGroup.LayoutParams.WRAP_CONTENT
    }
}
