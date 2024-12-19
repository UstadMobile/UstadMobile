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
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.viewmodel.UstadViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.kodein.di.instance
import kotlinx.serialization.json.Json



data class InviteViaContactChip(
    val text: String,
    val isValid: Boolean,
    val inviteType: Int
)

data class InviteViaContactUiState(
    private val fromContact: String? = null,
    val contactError: String? = null,
    val onSendClick: Boolean? = null,
    val chips: List<InviteViaContactChip> = emptyList()
)


class InviteViaContactViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, DEST_NAME) {
    private val parseInviteUseCase: ParseInviteUseCase by instance()
    private val contactToServerUseCase: ContactToServerUseCase by di.onActiveEndpoint().instance()
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
            _uiState.update { prev ->
                prev.copy(onSendClick = true)
            }

            val contacts = _uiState.value.chips

            if (contacts.isEmpty()) {
                val noContactFoundMessage = systemImpl.getString(MR.strings.no_contact_found)
                _uiState.update { prev ->
                    prev.copy(contactError = noContactFoundMessage)
                }
                onContactError(noContactFoundMessage)
                return@launch
            }

            val validContacts = contacts.filter { it.isValid }

            if (validContacts.isEmpty()) {
                val noValidContactFoundMessage = systemImpl.getString(MR.strings.no_valid_contact_found)
                _uiState.update { prev ->
                    prev.copy(contactError = noValidContactFoundMessage)
                }
                onContactError(noValidContactFoundMessage)
                return@launch
            }

            val result = contactToServerUseCase.invoke(
                validContacts.map { it.text },
                clazzUid,
                personRole,
                accountManager.currentUserSession.person.personUid
            )

            val invitation = Json.decodeFromString<InviteResult>(result)

            snackDispatcher.showSnackBar(Snack(invitation.inviteSent))

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

    fun onValueChanged() {
        _uiState.update { prev ->
            prev.copy(
                onSendClick = false,
                contactError = null
            )
        }
    }

    companion object {
        const val DEST_NAME = "invite_via_contact"
        const val ARG_ROLE = "person_role"
        const val ARG_CLAZZ_UID = "clazz_uid"
    }

   @Serializable
    data class InviteResult(
        val inviteSent: String
    )
}



