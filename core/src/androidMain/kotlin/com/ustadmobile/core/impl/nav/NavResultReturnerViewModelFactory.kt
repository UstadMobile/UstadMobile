package com.ustadmobile.core.impl.nav

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner

class NavResultReturnerViewModelFactory(
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null,
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T{
        return modelClass.getConstructor(
            UstadSavedStateHandle::class.java
        ).newInstance(SavedStateHandleAdapter(handle))
    }
}