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



/**
 * Used by the EpubActivity to scroll to an anchor (e.g. hash link). It will determine the position
 * of the given anchor and then call UstadEpub.scrollDown so that the recyclerview can scroll to
 * the given position.
 *
 * This is necessary because WebViews in EPUBs have their height set to wrap_content - so there is
 * no 'internal' scrolling within the webview itself.
 */
fun WebView.scrollToAnchor(anchorName: String) {
    loadUrl("""javascript:(function(anchorId) {
                        console.log("UstadEpub - scroll to " + anchorId);
                        var scrollFn = function() {
                            var anchorEl = document.getElementById(anchorId);
                            if(anchorEl != null) {
                                var boundingRect = anchorEl.getBoundingClientRect();
                                UstadEpub.scrollDown(Math.round(boundingRect.top));                            
                            }else {
                                console.error("UstadEpub: cannot find anchor: #" + anchorId);
                            }
                        };
                        
                        if(document.readyState == "complete") {
                            scrollFn();
                        }else {
                            document.addEventListener("readystatechange", function() {
                                if(document.readyState == "complete") {
                                    scrollFn();
                                }
                            });
                        }
                    })('$anchorName')""")
}