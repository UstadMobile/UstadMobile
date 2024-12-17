package com.ustadmobile.core.viewmodel.settings.localsharing

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadViewModel
import org.kodein.di.DI

class LocalSharingSettingsViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, DEST_NAME){

    companion object {

        const val DEST_NAME = "LocalSharingSettings"
    }
}