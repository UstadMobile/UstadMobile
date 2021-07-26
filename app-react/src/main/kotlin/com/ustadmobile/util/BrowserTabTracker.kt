package com.ustadmobile.util

import com.ustadmobile.navigation.UstadBackStackEntryJs
import com.ustadmobile.redux.ReduxAppStateManager
import com.ustadmobile.redux.ReduxAppStateManager.dispatch
import com.ustadmobile.redux.ReduxNavStackState
import kotlinx.browser.localStorage
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.json.Json
import kotlin.js.Date
import kotlin.js.json

@JsModule("browser-session-tabs-tracker")
@JsNonModule
external val tabTracker: dynamic

interface Tracker {
    val newSessionCreated: Boolean
    val tabId: Int
    val sessionInfo: Any?
}

@Serializable
data class BackStackEntry(var viewName: String = "",
                          var arguments: Map<String, String> = mapOf(),
                          var id: Int = 1)

object BrowserTabTracker: Tracker {

    private const val STATE_STORAGE_KEY = "key_tab_state_tracker"

    private val serializer = ListSerializer(BackStackEntry.serializer())

    private var instance: Tracker = tabTracker.BrowserTabTracker.unsafeCast<Tracker>()

    override val newSessionCreated: Boolean
        get() = instance.newSessionCreated

    override val tabId: Int
        get() = if(instance.sessionInfo == null) 1 else instance.tabId

    override val sessionInfo: Any?
        get() = instance.sessionInfo

    var navStackState: ReduxNavStackState
        get() {
            val tabStateList = getStoredStateList().filter { it.id == tabId }
            val navState = ReduxNavStackState()
            tabStateList.map {
                navState.stack.add(UstadBackStackEntryJs(it.viewName, it.arguments))
            }
            return navState
        }

        set(value) {
            val storedStateList = getStoredStateList()
            if(storedStateList.isNullOrEmpty()){
                localStorage.setItem(STATE_STORAGE_KEY,  "")
            }

            storedStateList.removeAll { it.id == tabId  && value.stack.isNotEmpty()}
            value.stack.forEach {
                storedStateList.add(BackStackEntry(it.viewName, it.arguments, tabId))
            }

            localStorage.setItem(STATE_STORAGE_KEY, Json.encodeToString(
                serializer, storedStateList))
        }


    //sessionIdGenerator, sessionStartedCallback, args is used by js code
    @Suppress("UNUSED_VARIABLE")
    fun init() {

        val sessionIdGenerator: () -> dynamic = {
            Date().getTime()
        }

        val sessionStartedCallback: (Any,Int) -> Unit = { _, _ ->
            localStorage.removeItem(STATE_STORAGE_KEY)
            updateState()
        }

        val newTabOpenedCallback: (Int) -> Unit = { _ ->
           updateState()
        }

        val args = json(
            "storageKey" to STATE_STORAGE_KEY,
            "sessionStartedCallback" to sessionStartedCallback,
            "sessionIdGenerator" to sessionIdGenerator,
            "newTabOpenedCallback" to newTabOpenedCallback
        )
        instance.asDynamic().initialize(args)
    }

    private fun updateState(){
        dispatch(ReduxAppStateManager.getCurrentState().navStack)
    }

    private fun getStoredStateList(): MutableList<BackStackEntry>{
        val storedStateList = localStorage.getItem(STATE_STORAGE_KEY)
        return if(storedStateList.isNullOrEmpty()){
            mutableListOf()
        }else{
            Json.decodeFromString(serializer, storedStateList).toMutableList()
        }
    }
}