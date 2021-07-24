package com.ustadmobile.util

import com.ustadmobile.redux.ReduxAppState
import kotlinx.browser.window
import kotlin.js.Date

@JsModule("browser-session-tabs-tracker")
@JsNonModule
external val tabTracker: dynamic

interface Tracker {
    val newSessionCreated: Boolean
    val tabId: Int
}

object BrowserTabTracker: Tracker {

    private var instance: Tracker = tabTracker.BrowserTabTracker.unsafeCast<Tracker>()


    //sessionIdGenerator, sessionStartedCallback, args is used by js code
    @Suppress("UNUSED_VARIABLE")
    fun init() {

        val sessionIdGenerator: () -> Any = {
            Date().getTime()
        }

        val newTabOpenedCallback: (Int) -> Unit = {
            if(window.asDynamic().tracker == js("undefined")){
                window.asDynamic().tracker = js("{}")
            }
            window.asDynamic().tracker[it] = js("{}")
        }

        instance.asDynamic().initialize(js("{storageKey: 'umTabTracker',sessionIdGenerator: sessionIdGenerator, newTabOpenedCallback:newTabOpenedCallback}"))
    }

    override val newSessionCreated: Boolean
        get() = instance.newSessionCreated

    override val tabId: Int
        get() = instance.tabId

    var appState: ReduxAppState
        get() = when (window.asDynamic().tracker) {
            js("undefined") -> {
                ReduxAppState()
            }
            else -> {
                window.asDynamic().tracker[tabId].unsafeCast<ReduxAppState>()
            }
        }
        set(value) {
            window.asDynamic().tracker[tabId] = value
        }
}