package com.ustadmobile.core.hooks

import kotlinx.browser.window
import org.w3c.dom.events.Event
import react.useEffect


/**
 * Run a synchronous callback if/when the page is about to unload.
 */
fun useOnUnloadEffect(
    block: () -> Unit
) {
    useEffect(dependencies = emptyArray()) {
        val evtListener: (Event) -> Unit = {
            block()
        }

        window.addEventListener("beforeunload", evtListener)

        cleanup {
            window.removeEventListener("beforeunload", evtListener)
        }

    }
}
