package com.ustadmobile.core.viewmodel.person.invitestudents

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.validEmail
import com.ustadmobile.core.util.isValidPhoneNumber
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import org.kodein.di.instance

data class InviteStudentsUiState(

    val fieldsEnabled: Boolean = true,

    val recipients: List<String> = emptyList(),

    val textField: String = "",

    val textFieldError: String? = null,

) {

    val addRecipientVisible: Boolean
        get() = (textField.validEmail()
                || isValidPhoneNumber(textField))
                && textFieldError.isNullOrBlank()

}

class InviteStudentsViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): UstadEditViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(InviteStudentsUiState())

    val uiState: Flow<InviteStudentsUiState> = _uiState.asStateFlow()

    private val impl: UstadMobileSystemImpl by instance()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = impl.getString(MessageID.invite_students),
                actionBarButtonState = ActionBarButtonUiState(
                    visible = _uiState.value.recipients.isNotEmpty(),
                    text = impl.getString(MessageID.invite),
                    onClick = this::onClickInvite
                )
            )
        }
    }

    fun onTextFieldChanged(text: String) {
        _uiState.update { prev ->
            prev.copy(
                textFieldError = if(prev.recipients.contains(text)) {
                    impl.getString(MessageID.duplicate)
                } else {
                    null
                },
                textField = text
            )
        }
    }

    fun onClickAddRecipient() {
        _uiState.update { prev ->
            prev.copy(
                recipients = prev.recipients.plus(prev.textField),
                textField = ""
            )
        }
        _appUiState.update { prev ->
            prev.copy(
                actionBarButtonState = ActionBarButtonUiState(
                    visible = _uiState.value.recipients.isNotEmpty(),
                    text = impl.getString(MessageID.invite),
                    onClick = this::onClickInvite
                )
            )
        }
    }

    fun onClickRemoveRecipient(recipient: String) {
        _uiState.update { prev ->
            prev.copy(recipients = prev.recipients.filter {
                it != recipient
            })
        }
    }

    private fun onClickInvite() {

    }

    companion object {

        const val DEST_NAME = "InviteStudents"

    }

}
