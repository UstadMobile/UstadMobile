package com.ustadmobile.util

import com.ustadmobile.core.controller.OnSearchSubmitted
import com.ustadmobile.mui.ext.targetInputValue
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.events.Event

/**
 * Manages search functionality, it dispatches the search query to the listening component
 */
class  SearchManager(private val viewId: String = "um-search") {

    private var searchView: Element? = null

    private var searchHandlerId = -1

    private var viewInitTimeoutId = -1

    var searchListener: OnSearchSubmitted? = null
        set(value) {
            viewInitTimeoutId = window.setTimeout({
                searchView = document.getElementById(viewId)
                searchView?.addEventListener("input", searchHandler)
            }, 1000)
            field = value
        }

    private var searchHandler:(Event) -> Unit = { event ->
        window.clearTimeout(searchHandlerId)
        searchHandlerId = window.setTimeout({
           searchListener?.onSearchSubmitted(event.targetInputValue)
        }, 500)
    }

    fun onDestroy(){
        window.removeEventListener("input",searchHandler)
        window.clearTimeout(searchHandlerId)
        window.clearTimeout(viewInitTimeoutId)
        searchListener = null
        searchView = null
    }

}