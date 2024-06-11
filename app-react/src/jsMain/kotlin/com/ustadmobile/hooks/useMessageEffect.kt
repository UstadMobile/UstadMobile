package com.ustadmobile.hooks

import react.useEffect
import web.events.EventType
import web.events.addEventListener
import web.events.removeEventListener
import web.messaging.MessageEvent
import web.window.Window
import web.window.window

/**
 *
 */
fun <T: Any> useMessageEffect(
    onMessage: (MessageEvent<T>) -> Unit,
) {
    useEffect(dependencies = emptyArray()) {
        val handler: (MessageEvent<T>) -> Unit = {
            onMessage(it)
        }

        window.addEventListener(EventType<MessageEvent<T>, Window>("message"), handler)

        cleanup {
            window.removeEventListener(EventType("message"), handler)
        }
    }
}