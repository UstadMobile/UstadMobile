package com.ustadmobile.core.navigation

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.door.lifecycle.MutableLiveData
import kotlinext.js.getOwnPropertyNames
import org.w3c.dom.History
import kotlin.js.Json
import kotlin.js.json
import kotlin.random.Random


/**
 * Simple adapter that will save key/value pairs to the history state.
 */
class SavedStateHandle2(
    private val history: History
): UstadSavedStateHandle {

    private val historyState: Json = history.state?.unsafeCast<Json>() ?: json()

    private val handleId: String

    init {
        val storedHandleId = historyState["_handleId"]?.toString()
        if(storedHandleId == null) {
            handleId = Random.nextInt().toString()
            historyState["_handleId"] = handleId
            history.replaceState(historyState, "")
        }else {
            handleId = storedHandleId
        }
    }

    override fun set(key: String, value: String?) {
        if(history.state?.asDynamic()?._handleId != handleId) {
            throw IllegalStateException("SavedState cannot save values after the user has changed page")
        }

        historyState[key] = value
        history.replaceState(historyState, "")
    }

    override fun get(key: String): String? {
        return historyState[key]?.toString()
    }

    override val keys: Set<String>
        get() = historyState.getOwnPropertyNames().toSet()

    @Deprecated("Should not be used anymore")
    override fun <T> getLiveData(key: String): MutableLiveData<T> {
        throw IllegalStateException("getLiveData not supported by SaveStateHandle2")
    }
}