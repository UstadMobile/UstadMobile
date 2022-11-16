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
 */
class UstadViewModelProviderFactory(
    private val di: DI,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null,
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return modelClass.getConstructor(
            DI::class.java, UstadSavedStateHandle::class.java
        ).newInstance(di, SavedStateHandleAdapter(handle))
    }
}
