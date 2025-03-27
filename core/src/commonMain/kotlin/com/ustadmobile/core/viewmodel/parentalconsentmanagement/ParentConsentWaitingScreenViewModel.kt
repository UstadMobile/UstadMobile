package com.ustadmobile.core.viewmodel.parentalconsentmanagement

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import kotlinx.coroutines.flow.update
import org.kodein.di.DI


class ParentConsentWaitingScreenViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
) : UstadEditViewModel(di, savedStateHandle, DEST_NAME) {
    init {
        _appUiState.update { prev ->
            prev.copy(
                navigationVisible = false,
                userAccountIconVisible = false,
                hideBottomNavigation = true
            )
        }
    }
    companion object {

        const val DEST_NAME = "ParentConsentWaitingScreen"

    }
}