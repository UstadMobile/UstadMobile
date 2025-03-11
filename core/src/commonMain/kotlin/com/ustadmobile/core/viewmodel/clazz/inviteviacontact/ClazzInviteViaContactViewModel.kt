package com.ustadmobile.core.viewmodel.clazz.inviteviacontact

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.hidekeyboard.HideKeyboardUseCase
import com.ustadmobile.core.domain.invite.ParseInviteUseCase
import com.ustadmobile.core.domain.invite.SendClazzInvitesUseCase
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.util.ext.onActiveLearningSpace
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.kodein.di.instance
import org.kodein.di.instanceOrNull


/**
 * @param text the actual text of the contact e.g. "person@email.com" "+12223312121" "@username"
 * @param isValid whether or not the contact is valid
 * @param inviteType the invite type as per ClazzInvite type constants
 */
data class InviteViaContactChip(
    val text: String,
    val isValid: Boolean,
    val inviteType: Int
)

data class ClazzInviteViaContactUiState(
    val contactError: String? = null,
    val chips: List<InviteViaContactChip> = emptyList(),
    val textFieldValue: String? = null,
)


class ClazzInviteViaContactViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, DEST_NAME) {
    private val parseInviteUseCase: ParseInviteUseCase by instance()
    private val sendClazzInvitesUseCase: SendClazzInvitesUseCase by
        di.onActiveLearningSpace().instance()
    private val clazzUid = savedStateHandle[ARG_CLAZZ_UID]?.toLong() ?: 0L
    private val personRole = savedStateHandle[ARG_ROLE]?.toLong() ?: 0L
    private val _uiState = MutableStateFlow(ClazzInviteViaContactUiState())

    val uiState: Flow<ClazzInviteViaContactUiState> = _uiState.asStateFlow()

    private val hideKeyboardUseCase: HideKeyboardUseCase? by instanceOrNull()

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
                    onClick = this@ClazzInviteViaContactViewModel::onClickSend
                )
            )
        }
    }

    fun onClickSend() {
        viewModelScope.launch {
            hideKeyboardUseCase?.invoke()

            val contacts = _uiState.value.chips

            if (contacts.isEmpty()) {
                val textField = _uiState.value.textFieldValue
                 if (!textField.isNullOrBlank()){
                     val parsedTextValue= parseInviteUseCase(textField)
                     sendContactsToServer(parsedTextValue)
                     return@launch
                 }

                val noContactFoundMessage = systemImpl.getString(MR.strings.no_contact_found)

                _uiState.update { prev ->
                    prev.copy(contactError = noContactFoundMessage)
                }

                onContactError(noContactFoundMessage)
                return@launch
            }

            sendContactsToServer(contacts)

        }
    }

    private suspend fun sendContactsToServer(contacts: List<InviteViaContactChip>) {
        val validContacts = contacts.filter { it.isValid }

        if (validContacts.isEmpty()) {
            val noValidContactFoundMessage = systemImpl.getString(MR.strings.no_valid_contact_found)
            _uiState.update { prev ->
                prev.copy(contactError = noValidContactFoundMessage)
            }
            onContactError(noValidContactFoundMessage)
            return
        }

        try {
            sendClazzInvitesUseCase(
                SendClazzInvitesUseCase.SendClazzInvitesRequest(
                    contacts = validContacts.map { it.text },
                    clazzUid = clazzUid,
                    role = personRole,
                    personUid = accountManager.currentUserSession.person.personUid
                )
            )
            snackDispatcher.showSnackBar(Snack(systemImpl.getString(MR.strings.invitations_sent)))

            navController.popBackStack(
                viewName = PersonListViewModel.DEST_NAME,
                inclusive = true
            )
        }catch(e: Throwable) {
            snackDispatcher.showSnackBar(
                Snack("${systemImpl.getString(MR.strings.error)}: ${e.message ?: ""}")
            )
        }
    }

    fun onContactError(error: String) {
        snackDispatcher.showSnackBar(Snack(error))
    }

    fun onClickChipSubmit(
        text: String,
    ):InviteViaContactChip {
        return _uiState.updateAndGet { prev ->
            prev.copy(
                chips = prev.chips + parseInviteUseCase.invoke(text)
            )
        }.chips.last()
    }

    /**
     * @param removedChipTexts list of the text of chips removed. In reality it is very likely a list of one,
     *        it is a list because in the unlikely event this is more than one, we do not want to
     *        trigger more than one state update.
     */
    fun onChipsRemoved(
        removedChipTexts: List<String>,
    ) {
        val newChips = _uiState.value.chips.filter {
            it.text !in removedChipTexts
        }

        _uiState.update { prev ->
            prev.copy(
                chips = newChips
            )
        }
    }

    fun onTextFieldValueChanged(newValue:String) {
        _uiState.update { prev ->
            prev.copy(
                textFieldValue = newValue
            )
        }
    }

    fun onValueChanged() {
        _uiState.update { prev ->
            prev.copy(
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



