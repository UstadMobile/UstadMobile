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
class UstadViewModelProviderFactory(
    private val di: DI,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle?,
    private val destinationName: String?,
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return if(destinationName != null){
            modelClass.getConstructor(
                DI::class.java, UstadSavedStateHandle::class.java, String::class.java,
            ).newInstance(di, SavedStateHandleAdapter(handle), destinationName)
        }else {
            modelClass.getConstructor(
                DI::class.java, UstadSavedStateHandle::class.java,
            ).newInstance(di, SavedStateHandleAdapter(handle))
        }
    }
}
