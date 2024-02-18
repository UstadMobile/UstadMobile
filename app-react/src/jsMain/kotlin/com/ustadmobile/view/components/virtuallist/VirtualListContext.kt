package com.ustadmobile.view.components.virtuallist

import react.StateInstance
import react.createContext
import tanstack.virtual.core.Virtualizer
import web.html.HTMLElement


typealias VirtualListState = StateInstance<VirtualListContextData>

data class VirtualListContextData(
    @Suppress("SpellCheckingInspection")
    val virtualizer: Virtualizer<HTMLElement, HTMLElement>,
    val allRows: List<VirtualListElement>,
    val reverseLayout: Boolean = false,
)

/**
 * VirtualListContext is used to allow the VirtualListOutlet to access the virtualizer and elements.
 *
 * As per https://react.dev/reference/react/useContext#updating-data-passed-via-context , to handle
 * updates, this is combined with state.
 */
internal val VirtualListContext = createContext<VirtualListState>()
