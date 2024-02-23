package com.ustadmobile.core.viewmodel.clazz.invitevialink

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.clipboard.SetClipboardStringUseCase
import com.ustadmobile.core.domain.makelink.MakeLinkUseCase
import com.ustadmobile.core.domain.share.ShareTextUseCase
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.clazz.joinwithcode.JoinWithCodeViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.kodein.di.instance
import org.kodein.di.instanceOrNull

data class InviteViaLinkUiState(

    val inviteLink: String? = null,

    val showShareLinkButton: Boolean = false,

)

class InviteViaLinkViewModel (
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(InviteViaLinkUiState())

    val uiState: Flow<InviteViaLinkUiState> = _uiState.asStateFlow()

    private val makeLinkUseCase: MakeLinkUseCase by di.onActiveEndpoint().instance()

    private val argInviteCode = savedStateHandle[ARG_INVITE_CODE]
        ?: throw IllegalArgumentException("no invite code")

    private val setClipboardStringUseCase: SetClipboardStringUseCase by instance()

    private val shareTextUseCase: ShareTextUseCase? by instanceOrNull()

    private val inviteLink: String = makeLinkUseCase(
        destName = JoinWithCodeViewModel.DEST_NAME,
        args = mapOf(
            ARG_INVITE_CODE to argInviteCode
        )
    )

    init {

        _appUiState.update { prev ->
            prev.copy(
                title = systemImpl.getString(MR.strings.invite_with_link)
            )
        }

        _uiState.update { prev ->
            prev.copy(
                inviteLink = inviteLink,
                showShareLinkButton = shareTextUseCase != null,
            )
        }
    }

    fun onClickCopy() {
        setClipboardStringUseCase(inviteLink)
        snackDispatcher.showSnackBar(Snack(systemImpl.getString(MR.strings.copied_to_clipboard)))
    }

    fun onClickShare(){
        shareTextUseCase?.invoke(inviteLink)
    }

    companion object {

        const val DEST_NAME = "InviteWithLink"

    }
}
