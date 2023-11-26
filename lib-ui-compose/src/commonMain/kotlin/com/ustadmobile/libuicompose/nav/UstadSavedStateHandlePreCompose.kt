package com.ustadmobile.libuicompose.nav

import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.libuicompose.util.ext.urlDecode
import kotlinx.coroutines.flow.MutableStateFlow
import moe.tlaster.precompose.navigation.BackStackEntry
import moe.tlaster.precompose.stateholder.SavedStateHolder

/**
 * Basic wrapper to implement SavedStateHandle key/value pair management using PreCompose. See
 * https://github.com/Tlaster/PreCompose/blob/master/docs/component/view_model.md
 *
 * @param savedKeys might be shared between multiple instances e.g. this is required when tabs are
 *        being used to avoid duplicate providers being registered with the savedstateholder.
 *
 */
class UstadSavedStateHandlePreCompose(
    private val savedStateHolder: SavedStateHolder,
    private val argsMap: Map<String, List<String>>?,
    private val savedKeys: MutableMap<String, SavedEntry> = mutableMapOf(),
) : UstadSavedStateHandle{

    //Used by ViewModel keys to generate a key that is unique for the given arguments
    internal val argsHash: Int = argsMap?.hashCode() ?: 0

    data class SavedEntry(
        val entry: SaveableStateRegistry.Entry,
        val stateFlow: MutableStateFlow<String?>,
    )

    constructor(backStackEntry: BackStackEntry): this(
        backStackEntry.savedStateHolder, backStackEntry.queryString?.map
    )

    override fun set(key: String, value: String?) {
        val entry = savedKeys[key]
        if(entry != null) {
            entry.stateFlow.value = value
        }else {
            val mutableStateFlow = MutableStateFlow(value)
            savedKeys[key] = SavedEntry(
                entry = savedStateHolder.registerProvider(key) { mutableStateFlow.value },
                stateFlow = mutableStateFlow
            )
        }
    }

    override fun get(key: String): String? {
        // consumeRestore won't do anything unless it is actually saved. We need to check our own
        // flow first.
        val savedState = savedKeys[key]?.stateFlow?.value
            ?: savedStateHolder.consumeRestored(key)?.toString()
        return  savedState?: argsMap?.get(key)?.firstOrNull()?.urlDecode()
    }

    override val keys: Set<String>
        get() = throw IllegalStateException("Not supported on precompose")

}