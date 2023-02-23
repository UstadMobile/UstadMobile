package com.ustadmobile.util

import web.dom.Element
import web.dom.document
import web.events.Event
import web.uievents.CLICK
import web.uievents.MouseEvent

/**
 * Manages Floating action button functionality
 */
class  FabManager(private val viewId: String = "um-fab", visible: Boolean = false) {

    private var fabView: Element? = null

    private var viewInitTimeoutId = -1

    private var clickEventHandler:(Event) -> Unit = {
        onClickListener?.invoke()
    }

    init {
        fabView = document.getElementById(viewId)
        fabView?.addEventListener(MouseEvent.CLICK, clickEventHandler)
    }

    var onClickListener: (() -> Unit)? = null
        get() = field
        set(value) {
            field = value
        }

    var visible: Boolean = visible
        set(value) {
            field = value
            updateIconAndFabText(value)
        }

    var icon: String? = null
        get() = field
        set(value){
            field = value
            updateIconAndFabText(visible)
        }

    var text: String? = null
        get() = field
        set(value) {
            field = value
            updateIconAndFabText(visible)
        }

    private fun updateIconAndFabText(visible: Boolean){
        fabView?.childNodes?.item(0)?.textContent = icon
        fabView?.childNodes?.item(1)?.textContent = text
        if(fabView!= null){
            val style = fabView?.asDynamic().style
            if(style != null)
                style.display = if(visible) "flex" else "none"
        }
    }

    fun onDestroy(){
        onClickListener = null
        fabView = null
    }

}