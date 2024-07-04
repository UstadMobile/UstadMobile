package com.ustadmobile.core.viewmodel.clazz.inviteviaContact

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.invite.ContactToServerUseCase
import com.ustadmobile.core.domain.invite.ParseInviteUseCase
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.viewmodel.UstadViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.instance


data class InviteViaContactChip(
    val text: String,
    val isValid: Boolean,
    val inviteType: Int
)

data class InviteViaContactUiState(
    private val fromContact: String? = null,
    val chips: List<InviteViaContactChip> = emptyList()
)


class InviteViaContactViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, DEST_NAME) {
    private val parseInviteUseCase: ParseInviteUseCase by instance()
    private val contactToServerUseCase: ContactToServerUseCase by instance()
    private val clazzUid = savedStateHandle[ARG_CLAZZ_UID]?.toLong() ?: 0L
    private val personRole = savedStateHandle[ARG_ROLE]?.toLong() ?: 0L
    private var _uiState = MutableStateFlow(InviteViaContactUiState())

    val uiState: Flow<InviteViaContactUiState> = _uiState.asStateFlow()


    init {
        _appUiState.update {
            AppUiState(
                title = systemImpl.getString(MR.strings.invite_to_course),
                hideBottomNavigation = true,
            )
        }

        _appUiState.update { prev ->
            prev.copy(
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MR.strings.send),
                    onClick = this@InviteViaContactViewModel::OnClickSend
                )
            )
        }
    }

    fun OnClickSend() {
        viewModelScope.launch {
            contactToServerUseCase.invoke(
                _uiState.value.chips.map { it.text },
                clazzUid,
                personRole,
                accountManager.currentUserSession.person.personUid
            )
        }
    }

    fun onContactError(error: String) {
        snackDispatcher.showSnackBar(Snack(error))
    }

    fun onClickChipSubmit(
        text: String,
    ) {
        _uiState.update { prev ->
            prev.copy(
                chips = prev.chips + parseInviteUseCase.invoke(text)
            )
        }
    }

    fun onChipRemoved(
        text: String,
    ) {
        val newChips = _uiState.value.chips.filter { it.text != text }
        _uiState.update { prev ->
            prev.copy(
                chips = newChips
            )
        }
    }

    companion object {
        const val DEST_NAME = "invite_via_contact"
        const val ARG_ROLE = "person_role"
        const val ARG_CLAZZ_UID = "clazz_uid"
    }


}



