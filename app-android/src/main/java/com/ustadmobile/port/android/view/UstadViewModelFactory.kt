package com.ustadmobile.port.android.view

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.ustadmobile.core.impl.nav.SavedStateHandleAdapter
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import org.kodein.di.DI

/**
 * Base ViewModel factory adapter. Uses reflection to instantiate the required ViewModel. Will always
 * pass the DI and UstadSavedStateHandle
 *
 * @param destinationName if provided, then look for a constructor where the destinationName can be
 * explicitly provided. Otherwise, use only DI and SavedStateHandle constructor
 */
class UstadViewModelProviderFactory<T: ViewModel>(
    private val di: DI,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle?,
    private val vmFactory: (DI, UstadSavedStateHandle) -> T,
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return vmFactory(di, SavedStateHandleAdapter(handle)) as T
    }
}
