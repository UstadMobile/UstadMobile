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
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import org.kodein.di.DI
import org.kodein.di.instance

data class InviteStudentsUiState(

    val fieldsEnabled: Boolean = true,

    val recipients: List<String> = emptyList(),

    val textField: String = "",

    val textFieldError: String? = null,

    val addRecipientVisible: Boolean = false
)

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

        _uiState.updateAndGet { prev ->
            prev.copy(
                addRecipientVisible = (prev.textField.validEmail()
                || isValidPhoneNumber(di = di, str = prev.textField))
                && prev.textFieldError.isNullOrBlank()
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
                textField = text,
                addRecipientVisible = (isValidEmail(text)
                        || isValidPhoneNumber(di = di, str = text))
                        && prev.textFieldError.isNullOrBlank()
            )
        }
    }

    private fun isValidEmail(str: String): Boolean {
        if(str.contains("@")){
            val splittedStr = str.split("@", limit = 2)
            if (!(splittedStr[0].contains(" ")
                        || splittedStr[0].isBlank()
                        || splittedStr[1].contains(" ")
                        || splittedStr[1].isBlank())){
                return true
            }
        }
        return false
    }

    fun onClickAddContact(text: String) {
        val newState = _uiState.updateAndGet { prev ->
            prev.copy(
                recipients = prev.recipients.plus(text)
            )
        }

        saveState(newState)

        updateAppUiState(newState)
    }

    fun onClickAddRecipient() {
        val newState = _uiState.updateAndGet { prev ->
            prev.copy(
                recipients = prev.recipients.plus(prev.textField),
                textField = ""
            )
        }

        saveState(newState)

        updateAppUiState(newState)

    }

    fun onClickRemoveRecipient(recipient: String) {
        val newState = _uiState.updateAndGet { prev ->
            prev.copy(recipients = prev.recipients.filter {
                it != recipient
            })
        }

        saveState(newState)

        updateAppUiState(newState)

    }

    private fun saveState(newState: InviteStudentsUiState){
        scheduleEntityCommitToSavedState(
            entity = newState.recipients,
            serializer = ListSerializer(String.serializer()),
            commitDelay = 200,
        )
    }

    private fun updateAppUiState(newState: InviteStudentsUiState){
        _appUiState.update { prev ->
            prev.copy(
                actionBarButtonState = ActionBarButtonUiState(
                    visible = newState.recipients.isNotEmpty(),
                    text = impl.getString(MessageID.invite),
                    onClick = this::onClickInvite
                )
            )
        }
    }

    private fun onClickInvite() {
        _uiState.update { prev ->
            prev.copy(fieldsEnabled = false)
        }
    }

    companion object {

        const val DEST_NAME = "InviteStudents"

    }

}
