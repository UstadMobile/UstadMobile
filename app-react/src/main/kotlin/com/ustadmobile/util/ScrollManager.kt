package com.ustadmobile.util

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.events.Event

/**
 * Manages scroll functionality
 */
class  ScrollManager(private val viewId: String, private val triggerThreshold:Int = 100,
                     private val triggerOnDownScroll: Boolean = true,
                     private val delay: Int = 200) {

    private var scrollElement: Element? = null

    private var scrollHandlerTimeOutId =  -1

    private var lastScrollTop = 0

    private var scrollHandler:(Int) -> Unit = {
        val triggerCondition = (if(triggerOnDownScroll) it > lastScrollTop else lastScrollTop > it)
        scrollListener?.let { listener -> listener(triggerCondition && it >= triggerThreshold, it) }
        lastScrollTop = it
    }

    var scrollListener: ((Boolean, Int) -> Unit?)? = null
        set(value) {
            if(value != null){
                scrollElement = document.getElementById(viewId)
                scrollElement?.addEventListener("scroll", scrollEventCallback)
            }
            field = value
        }

    private var scrollEventCallback :(Event) -> Unit = {
        val target = it.target.asDynamic()
        val scrollPercentage = js("Math.ceil((target.scrollTop/target.scrollHeight) * 100)")
            .toString().toInt()
        if(scrollHandlerTimeOutId != -1) window.clearTimeout(scrollHandlerTimeOutId)
        scrollHandlerTimeOutId = window.setTimeout(scrollHandler,delay,
            scrollPercentage)
    }

    fun onDestroy(){
        scrollElement?.removeEventListener("scroll", scrollEventCallback)
        scrollElement = null
        lastScrollTop = 0
        window.clearTimeout(scrollHandlerTimeOutId)
        scrollHandlerTimeOutId = -1
        scrollListener = null
    }

}