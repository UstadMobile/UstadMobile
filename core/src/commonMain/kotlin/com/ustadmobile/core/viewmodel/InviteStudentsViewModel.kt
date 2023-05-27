package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.validEmail
import com.ustadmobile.core.util.isValidPhoneNumber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.kodein.di.DI

data class InviteStudentsUiState(

    val fieldsEnabled: Boolean = true,

    val recipients: List<String> = emptyList(),

    val textField: String = "",

    val classInvitationLink: String = "",

) {

    val addRecipientVisible: Boolean
        get() = textField.validEmail() && isValidPhoneNumber(textField)

}

class InviteStudentsViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): UstadEditViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(InviteStudentsUiState())

    val uiState: Flow<InviteStudentsUiState> = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = systemImpl.getString(MessageID.invite_students),
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MessageID.invite),
                    onClick = this::onClickInvite
                )
            )
        }
    }

    fun onTextFieldChanged(text: String) {
        _uiState.update { prev ->
            prev.copy(textField = text)
        }
    }

    fun onClickAddRecipient() {
        _uiState.update { prev ->
            prev.copy(
                recipients = prev.recipients.plus(prev.textField),
                textField = ""
            )
        }
    }

    fun onClickRemoveRecipient(recipient: String) {
        _uiState.update { prev ->
            prev.copy(recipients = _uiState.value.recipients.filter {
                it != recipient
            })
        }
    }

    fun onClickInvite() {

    }

    fun onClickShareLink() {

    }
    fun onClickCopyLink() {

    }

    companion object {

        const val DEST_NAME = "InviteStudents"

    }

}
