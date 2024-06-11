package com.ustadmobile.hooks

import react.useEffect
import web.dom.document
import web.events.Event
import web.events.EventType
import web.events.addEventListener
import web.events.removeEventListener
import web.window.window

/**
 * Listen for window focus changes to trigger a callback e.g. to track time the user is actively
 * using the page.
 */
fun useWindowFocusedEffect(
    onFocusChanged: (Boolean) -> Unit,
) {
    useEffect(dependencies = emptyArray()) {
        onFocusChanged(document.hasFocus())

        val focusListener: (Event) -> Unit = {
            onFocusChanged(true)
        }

        val blurListener: (Event) -> Unit = {
            onFocusChanged(false)
        }

        window.addEventListener(EventType("focus"), focusListener)
        window.addEventListener(EventType("blur"), blurListener)

        cleanup {
            window.removeEventListener(EventType("focus"), focusListener)
            window.removeEventListener(EventType("blue"), blurListener)
        }
    }




}