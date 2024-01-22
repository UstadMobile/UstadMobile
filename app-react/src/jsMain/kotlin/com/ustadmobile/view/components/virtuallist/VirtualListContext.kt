package com.ustadmobile.view.components.virtuallist

import react.createContext
import tanstack.virtual.core.Virtualizer
import web.html.HTMLElement


internal data class VirtualListContextData(
    @Suppress("SpellCheckingInspection")
    val virtualizer: Virtualizer<HTMLElement, HTMLElement>,
    val allRows: List<VirtualListElement>,
    val reverseLayout: Boolean = false,
)

/**
 * VirtualListContext is used to allow the VirtualListOutlet to access the virtualizer and elements
 */
internal val VirtualListContext = createContext<VirtualListContextData>()
