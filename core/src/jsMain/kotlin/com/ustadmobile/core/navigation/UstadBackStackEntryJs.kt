package com.ustadmobile.core.navigation

import com.ustadmobile.core.impl.nav.UstadBackStackEntry
import kotlinx.browser.sessionStorage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class BackStackEntryInfo(
    val viewName: String,
    val arguments: Map<String, String>,
    val jsViewUri: String,
    val stateHandle: Map<String, String>,
)


class UstadBackStackEntryJs(
    override val viewName: String,
    override val arguments: Map<String, String>,
    //jsViewUri is stored as a string so NavControllerJs can find it without worrying about the order of the arguments map
    internal val jsViewUri: String,
    private val storageKey: String,
    private val json: Json?,
    stateHandleValues: Map<String, String>? = null,
    saveToStorageOnInit: Boolean = true
) : UstadBackStackEntry, UstadSavedStateHandleJs.CommitListener{

    override val savedStateHandle = UstadSavedStateHandleJs(stateHandleValues, this)

    init {
        if(saveToStorageOnInit)
            saveToSessionStorage()
    }

    override fun onCommit() {
        saveToSessionStorage()
    }

    private fun saveToSessionStorage() {
        val jsonVal = json ?: throw IllegalStateException("StackEntry JSON is null")
        val entryInfo = BackStackEntryInfo(viewName, arguments, jsViewUri,
            savedStateHandle.currentValues)
        sessionStorage.setItem(storageKey,
            jsonVal.encodeToString(BackStackEntryInfo.serializer(), entryInfo))
    }

    companion object {

        fun loadFromSessionStorage(storageKey: String, json: Json) : UstadBackStackEntryJs {
            val storedJson = sessionStorage.getItem(storageKey)
                ?: throw IllegalStateException("loadFromSessionStorage: $storageKey is empty")
            val storedEntry = json.decodeFromString(BackStackEntryInfo.serializer(), storedJson)
            return UstadBackStackEntryJs(storedEntry.viewName, storedEntry.arguments,
                storedEntry.jsViewUri, storageKey, json, storedEntry.stateHandle,
            false)
        }

    }

}