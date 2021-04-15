package com.ustadmobile.util

import com.ccfraser.muirwik.components.targetInputValue
import com.ustadmobile.core.controller.OnSearchSubmitted
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.events.Event

/**
 * Manages search functionality, it dispatches the search query to the listening component
 */
class  SearchManager(private val viewId: String) {

    private var searchView: Element? = null

    private var searchHandlerId = 0

    var searchListener: OnSearchSubmitted? = null
    set(value) {
        window.setTimeout({
            searchView = document.getElementById(viewId)
            searchView?.addEventListener("input", searchHandler)
        }, 1000)
        field = value
    }

    private var searchHandler:(Event) -> Unit = {
        window.clearTimeout(searchHandlerId)
        searchHandlerId = window.setTimeout({
           searchListener?.onSearchSubmitted(it.targetInputValue)
        }, 500)
    }

    fun onDestroy(){
        window.removeEventListener("input",searchHandler)
        window.clearTimeout(searchHandlerId)
        searchListener = null
        searchView = null
    }

}