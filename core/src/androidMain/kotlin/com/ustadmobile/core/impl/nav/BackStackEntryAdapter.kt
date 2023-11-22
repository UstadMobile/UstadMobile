package com.ustadmobile.core.impl.nav

import androidx.navigation.NavBackStackEntry
import com.ustadmobile.core.util.ext.toStringMap

class BackStackEntryAdapter(private val backStackEntry: NavBackStackEntry,
                            override val viewName: String) : UstadBackStackEntry {

    private val savedStateAdapter: UstadSavedStateHandle by lazy {
        UstadSavedStateHandleAndroid(backStackEntry)
    }

    override val savedStateHandle: UstadSavedStateHandle
        get() = savedStateAdapter

    override val arguments: Map<String, String>
        get() = backStackEntry.arguments?.toStringMap() ?: mapOf()

}