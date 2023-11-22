package com.ustadmobile.libuicompose

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner

class ViewModelFactory<T: ViewModel>(
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle?,
    private val vmFactory: () -> T,
): AbstractSavedStateViewModelFactory(owner, defaultArgs)  {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return vmFactory() as T
    }
}
