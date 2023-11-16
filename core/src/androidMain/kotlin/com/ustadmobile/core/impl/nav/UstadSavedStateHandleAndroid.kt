package com.ustadmobile.core.impl.nav

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry

class UstadSavedStateHandleAndroid(
    val savedStateHandle: SavedStateHandle,
    val args: Bundle?
) : UstadSavedStateHandle{

    constructor(backStackEntry: NavBackStackEntry) : this(
        savedStateHandle = backStackEntry.savedStateHandle,
        args = backStackEntry.arguments
    )

    override fun set(key: String, value: String?) {
        savedStateHandle[key] = value
    }

    override fun get(key: String): String? {
        return savedStateHandle[key] ?: args?.getString(key)
    }

    override val keys: Set<String>
        get() = savedStateHandle.keys() + (args?.keySet() ?: emptySet())

}
