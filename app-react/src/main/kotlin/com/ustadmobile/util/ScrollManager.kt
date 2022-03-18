package com.ustadmobile.util

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.events.Event

/**
 * Manages scroll behaviours with trigger events, currently is being used to mimic an endless
 * scrolling behaviour on iframes where views are being paginated as root view scrolls.
 *
 * @param viewToObserve Id of the view to be watched for scroll events (Div)
 * @param triggerOnDownScroll Flag to decide which scroll direction should trigger the event
 * @param triggerThreshold Percentage on which the event should be triggered
 * @param delay How long event should wait to be triggered
 */
class  ScrollManager(
    private val viewToObserve: String,
    private val triggerThreshold:Int = 50,
    private val triggerOnDownScroll: Boolean = true,
    private val delay: Int = 200) {

    private var scrollElement: Element? = null

    private var scrollHandlerTimeOutId =  -1

    private var lastScrollPercentage = 0

    private var scrollHandler:(Int) -> Unit = { scrollPercentage ->
        val triggerPageChangeEvent = when {
            triggerOnDownScroll -> scrollPercentage > lastScrollPercentage
            else -> lastScrollPercentage > scrollPercentage
        }
        scrollListener?.invoke(triggerPageChangeEvent
                && scrollPercentage >= triggerThreshold, scrollPercentage)
        lastScrollPercentage = scrollPercentage
    }

    var scrollListener: ((Boolean, Int) -> Unit?)? = null
        set(value) {
            if(value != null){
                scrollElement = document.getElementById(viewToObserve)
                scrollElement?.addEventListener("scroll", scrollEventCallback)
            }
            field = value
        }

    //target is used by js code
    @Suppress("UNUSED_VARIABLE")
    private var scrollEventCallback :(Event) -> Unit = {scrollEvent ->
        val target = scrollEvent.target.asDynamic()
        val scrollPercentage = js("Math.ceil((target.scrollTop/target.scrollHeight) * 100)")
            .toString().toInt()
        if(scrollHandlerTimeOutId != -1) window.clearTimeout(scrollHandlerTimeOutId)
        scrollHandlerTimeOutId = window.setTimeout(scrollHandler,delay, scrollPercentage)
    }

    fun onDestroy(){
        scrollElement?.removeEventListener("scroll", scrollEventCallback)
        scrollElement = null
        lastScrollPercentage = 0
        window.clearTimeout(scrollHandlerTimeOutId)
        scrollHandlerTimeOutId = -1
        scrollListener = null
    }

}