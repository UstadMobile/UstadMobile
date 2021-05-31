package com.ustadmobile.core.impl.nav

import androidx.navigation.NavBackStackEntry

class BackStackEntryAdapter(private val backStackEntry: NavBackStackEntry,
                            override val viewName: String) : UstadBackStackEntry {

    private val savedStateAdapter: UstadSavedStateHandle by lazy {
        SavedStateHandleAdapter(backStackEntry.savedStateHandle)
    }

    override val savedStateHandle: UstadSavedStateHandle
        get() = savedStateAdapter

}