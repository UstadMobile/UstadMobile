package com.ustadmobile.hooks

import react.useEffect
import react.useState
import web.events.Event
import web.events.EventHandler
import web.events.addEventListener
import web.events.removeEventListener
import web.window.Window
import web.window.resize
import web.window.window

data class UseWindowSizeState(val width: Int, val height: Int)

fun useWindowSize(): UseWindowSizeState {
    var state: UseWindowSizeState by useState {
        UseWindowSizeState(window.innerWidth, window.innerHeight)
    }

    useEffect {
        val handleResize: EventHandler<Event, Window> = EventHandler {
            state = UseWindowSizeState(window.innerWidth, window.innerHeight)
        }

        window.addEventListener(Event.Companion.resize(), handleResize)

        cleanup {
            window.removeEventListener(Event.Companion.resize(), handleResize)
        }
    }

    return state
}
