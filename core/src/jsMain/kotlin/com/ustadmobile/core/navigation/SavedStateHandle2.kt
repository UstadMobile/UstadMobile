package com.ustadmobile.core.navigation

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.door.lifecycle.MutableLiveData
import js.core.toSet
import kotlinext.js.getOwnPropertyNames
import org.w3c.dom.History
import web.url.URLSearchParams
import kotlin.js.Json
import kotlin.js.json
import kotlin.random.Random


/**
 * Simple adapter that will save key/value pairs to the history state.
 */
class SavedStateHandle2(
    private val history: History,
    private val searchParams: URLSearchParams,
): UstadSavedStateHandle {

    private val handleId: String

    private fun History.setStateKey(key: String, value: String) {
        val newState = state?.unsafeCast<Json>() ?: json()
        newState[key] = value
        replaceState(newState, "")
    }

    init {
        val storedHandleId = get(KEY_HANDLE_ID)
        if(storedHandleId == null) {
            handleId = Random.nextInt().toString()
            history.setStateKey(KEY_HANDLE_ID, handleId)
        }else {
            handleId = storedHandleId
        }
    }

    override fun set(key: String, value: String?) {
        if(history.state?.asDynamic()?._handleId != handleId) {
            throw IllegalStateException("SavedState cannot save values after the user has changed page")
        }

        if(value != null) {
            history.setStateKey(key, value)
        }
    }

    override fun get(key: String): String? {
        return history.state?.unsafeCast<Json>()?.get(key)?.toString()
            ?: searchParams[key]
    }

    override val keys: Set<String>
        get() {
            val stateKeys = history.state?.getOwnPropertyNames()?.toSet() ?: emptySet()
            val searchParamKeys = searchParams.keys().toSet()
            return stateKeys + searchParamKeys
        }

    @Deprecated("Should not be used anymore")
    override fun <T> getLiveData(key: String): MutableLiveData<T> {
        throw IllegalStateException("getLiveData not supported by SaveStateHandle2")
    }

    companion object {

        private const val KEY_HANDLE_ID = "_handleId"

    }
}