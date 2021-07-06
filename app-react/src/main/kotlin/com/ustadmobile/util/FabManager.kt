package com.ustadmobile.util

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.events.Event
import org.w3c.dom.get

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
        fabView?.addEventListener("click", clickEventHandler)
    }

    var onClickListener: (() -> Unit)? = null
        get() = field
        set(value) {
            field = value
        }

    var visible: Boolean = visible
        set(value) {
            field = value
            if(fabView!= null){
                val style = fabView?.asDynamic().style
                if(style != null)
                    style.display = if(value) "flex" else "none"
            }
        }

    var icon: String? = null
        get() = field
        set(value){
            field = value
            updateIconAndFabText()
        }

    var text: String? = null
        get() = field
        set(value) {
            field = value
            updateIconAndFabText()
        }

    private fun updateIconAndFabText(){
        if(!text.isNullOrBlank() && !icon.isNullOrBlank()){
            val fabInnerHTML = """
            <span class="MuiFab-label">
                <span class="material-icons MuiIcon-root sc-gKAaRy" aria-hidden="true">
                    $icon
                </span>
                $text
            """.trimIndent()
            fabView?.getElementsByClassName("MuiFab-label")
                ?.get(0)?.parentElement?.innerHTML = fabInnerHTML
        }else{
            visible = false
        }
    }

    fun onDestroy(){
        window.removeEventListener("click",clickEventHandler)
        window.clearTimeout(viewInitTimeoutId)
        onClickListener = null
        fabView = null
    }

    companion object {
        private const val VIEW_INIT_TIMEOUT = 1000
    }

}