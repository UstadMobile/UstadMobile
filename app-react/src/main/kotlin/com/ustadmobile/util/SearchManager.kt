package com.ustadmobile.util

import com.ustadmobile.core.controller.OnSearchSubmitted
import com.ustadmobile.mui.ext.targetInputValue
import web.dom.Element
import web.dom.document
import web.events.Event
import web.events.EventType
import web.timers.Timeout
import web.timers.clearTimeout
import web.timers.setTimeout
import web.window.window

/**
 * Manages search functionality, it dispatches the search query to the listening component
 */
class  SearchManager(private val viewId: String = "um-search") {

    private var searchView: Element? = null

    private var searchHandlerId: Timeout? = null

    private var viewInitTimeoutId: Timeout? = null

    var searchListener: OnSearchSubmitted? = null
        set(value) {
            viewInitTimeoutId = setTimeout({
                searchView = document.getElementById(viewId)
                searchView?.addEventListener(EventType("input"), searchHandler)
            }, 1000)
            field = value
        }

    private var searchHandler:(Event) -> Unit = { event ->
        viewInitTimeoutId?.also { clearTimeout(it) }
        searchHandlerId = setTimeout({
           searchListener?.onSearchSubmitted(event.targetInputValue)
        }, 500)
    }

    fun onDestroy(){
        window.removeEventListener(EventType("input"),searchHandler)
        searchHandlerId?.also { clearTimeout(it) }
        viewInitTimeoutId?.also { clearTimeout(it) }
        searchListener = null
        searchView = null
    }

}