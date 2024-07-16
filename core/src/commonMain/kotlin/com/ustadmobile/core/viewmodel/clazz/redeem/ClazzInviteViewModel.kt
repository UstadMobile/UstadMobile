package com.ustadmobile.core.viewmodel.clazz.redeem

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadViewModel
import kotlinx.coroutines.flow.update
import org.kodein.di.DI

class ClazzInviteViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val argInviteCode = savedStateHandle[ARG_INVITE_CODE]
        ?: throw IllegalArgumentException("no invite code")
    init {
        _appUiState.update { prev ->
            prev.copy(
                hideBottomNavigation = true,
            )
        }
        savedStateHandle[UstadView.ARG_RESULT_DEST_KEY]
    }
    companion object {

        const val DEST_NAME = "clazz_redeem"
        const val ARG_INVITE = "inviteCode"
    }
}