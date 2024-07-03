package com.ustadmobile.core.viewmodel.clazz.redeem

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadViewModel
import org.kodein.di.DI

class ClazzInviteViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, DEST_NAME) {
    companion object {

        const val DEST_NAME = "clazz_redeem"
        const val ARG_INVITE = "inviteCode"
    }
}