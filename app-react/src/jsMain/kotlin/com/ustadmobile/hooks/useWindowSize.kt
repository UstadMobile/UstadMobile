package com.ustadmobile.hooks

import react.useEffect
import react.useState
import web.events.Event
import web.events.EventHandler
import web.window.RESIZE
import web.window.window

data class UseWindowSizeState(val width: Int, val height: Int)

fun useWindowSize(): UseWindowSizeState {
    var state: UseWindowSizeState by useState {
        UseWindowSizeState(window.innerWidth, window.innerHeight)
    }

    useEffect {
        val handleResize: EventHandler<Event> = {
            state = UseWindowSizeState(window.innerWidth, window.innerHeight)
        }

        window.addEventListener(Event.Companion.RESIZE, handleResize)

        cleanup {
            window.removeEventListener(Event.Companion.RESIZE, handleResize)
        }
    }

    return state
}
