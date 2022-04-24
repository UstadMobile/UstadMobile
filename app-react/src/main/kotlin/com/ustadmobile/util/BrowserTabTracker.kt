package com.ustadmobile.util

import com.ustadmobile.redux.ReduxAppStateManager.dispatch
import com.ustadmobile.redux.ReduxAppStateManager.getCurrentState
import com.ustadmobile.redux.ReduxNavStackState
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
data class TabState(
    var viewName: String = "",
    var arguments: Map<String, String> = mapOf(),
    var id: Int = 1)

/**
 * This will handle and track active browsers tabs running the app,
 * it will also make sure there is one tab opened at a time since we
 * do not support multiple tabs running at the same time.
 */
object BrowserTabTracker {

    private const val KEY_STATE_STORAGE = "key_tab_state_tracker"

    private const val KEY_ACTIVE_TAB = "key_active"

    private val serializer = ListSerializer(TabState.serializer())

    var activeTabRunning: Boolean = false
        get() = field
        set(value) {
            field = value
        }

    @Deprecated("This should not be used now that the nav stack manages its own persistence")
    var navStackState: ReduxNavStackState
        get() {
            val tabStateList = getStoredTabStateList()
            val navState = ReduxNavStackState()
            tabStateList.map {
//This is no longer used. Pending check with original developer.
//                navState.stack.add(UstadBackStackEntryJs(it.viewName, it.arguments, "",
//                    "ustadnav", null, saveToStorageOnInit = false))
            }
            return navState
        }

        set(value) {
            val storedStateList = getStoredTabStateList()
            if(storedStateList.isNullOrEmpty()){
                localStorage.setItem(KEY_STATE_STORAGE,  "")
            }

            storedStateList.removeAll { value.stack.isNotEmpty()}
            value.stack.forEach {
                storedStateList.add(TabState(it.viewName, it.arguments))
            }

            localStorage.setItem(KEY_STATE_STORAGE, Json.encodeToString(
                serializer, storedStateList))
        }


    @Suppress("UNUSED_VARIABLE")
    fun init(onExtraTabDetected: ((Boolean) -> Unit)) {
        activeTabRunning = localStorage.getItem(KEY_ACTIVE_TAB)?.toBoolean() ?: false
        if(!activeTabRunning){
            localStorage.setItem(KEY_ACTIVE_TAB, true.toString())
            activeTabRunning = true
            onExtraTabDetected(false)
        }else {
            onExtraTabDetected(true)
        }
        dispatch(getCurrentState().navStack)
        window.onbeforeunload = {
            localStorage.removeItem(KEY_ACTIVE_TAB)
            null
        }
    }

    private fun getStoredTabStateList(): MutableList<TabState>{
        val storedStateList = localStorage.getItem(KEY_STATE_STORAGE)
        return if(storedStateList.isNullOrEmpty()){
            mutableListOf()
        }else{
            Json.decodeFromString(serializer, storedStateList).toMutableList()
        }
    }
}