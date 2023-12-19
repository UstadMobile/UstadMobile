package com.ustadmobile.core.viewmodel.person.accountedit

import com.ustadmobile.core.MR
import com.ustadmobile.core.account.AuthManager
import com.ustadmobile.core.domain.validateusername.ValidateUsernameUseCase
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Role
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

@Serializable
data class PersonUsernameAndPasswordModel(

    val username: String? = "",

    val currentPassword: String? = "",

    val newPassword: String? = "",

)

data class PersonAccountEditUiState(

    val personAccount: PersonUsernameAndPasswordModel? = PersonUsernameAndPasswordModel(),

    val usernameError: String? = null,

    val currentPasswordError: String? = null,

    val newPasswordError: String? = null,

    val errorMessage: String? = null,

    val fieldsEnabled: Boolean = false,
) {
    val usernameVisible: Boolean
        get() = personAccount?.username != null

    val currentPasswordVisible: Boolean

        get() = personAccount?.currentPassword != null

    val hasErrors: Boolean
        get() = usernameError != null || currentPasswordError != null || newPasswordError != null
}

class PersonAccountEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): UstadEditViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(PersonAccountEditUiState())

    val uiState: Flow<PersonAccountEditUiState> = _uiState.asStateFlow()

    private val authManager: AuthManager by on(accountManager.activeEndpoint).instance()

    private val validateUsernameUseCase = ValidateUsernameUseCase()

    init {
        _appUiState.value = AppUiState(
            loadingState = LoadingUiState.INDETERMINATE,
        )

        viewModelScope.launch {
            loadEntity(
                serializer = PersonUsernameAndPasswordModel.serializer(),
                onLoadFromDb = { db ->
                    val person = db.personDao.findByUidAsync(entityUidArg)
                    val hasResetPermission = db.personDao.personHasPermissionAsync(
                        activeUserPersonUid, entityUidArg, Role.PERMISSION_RESET_PASSWORD
                    )

                    if(person == null) {
                        null
                    }else {
                        PersonUsernameAndPasswordModel(
                            username = if(person.username == null) "" else null,
                            currentPassword = if(hasResetPermission) null else "",
                            newPassword = "",
                        )
                    }
                },
                makeDefault = {
                    null
                },
                uiUpdate = {
                    _uiState.update { prev ->
                        prev.copy(
                            personAccount = it
                        )
                    }
                }
            )
            _appUiState.update { prev ->
                prev.copy(
                    actionBarButtonState = ActionBarButtonUiState(
                        visible = true,
                        text = systemImpl.getString(MR.strings.save),
                        onClick = this@PersonAccountEditViewModel::onClickSave
                    ),
                    loadingState = LoadingUiState.NOT_LOADING,
                )
            }
            _uiState.update { prev -> prev.copy(fieldsEnabled = true) }
        }
    }

    fun onEntityChanged(entity: PersonUsernameAndPasswordModel?) {
        _uiState.update { prev ->
            prev.copy(
                personAccount = entity,
                usernameError = if(prev.usernameError != null && prev.personAccount?.username == entity?.username) {
                    prev.usernameError
                }else {
                    null
                },
                currentPasswordError = if(prev.currentPasswordError != null
                    && prev.personAccount?.currentPassword == entity?.currentPassword) {
                    prev.currentPasswordError
                }else {
                    null
                },
                newPasswordError = if(prev.newPasswordError != null &&
                    prev.personAccount?.newPassword == entity?.newPassword) {
                    prev.newPasswordError
                }else {
                    null
                }
            )
        }

        scheduleEntityCommitToSavedState(
            entity = entity,
            serializer = PersonUsernameAndPasswordModel.serializer(),
            commitDelay = 200
        )
    }


    fun onClickSave() {
        if(loadingState == LoadingUiState.INDETERMINATE)
            return

        val entity = _uiState.value.personAccount ?: return

        loadingState = LoadingUiState.INDETERMINATE
        _uiState.update { prev ->
            prev.copy(
                fieldsEnabled = false
            )
        }
        viewModelScope.launch {
            if(entity.username != null && entity.username.isBlank()) {
                _uiState.update { prev ->
                    prev.copy(usernameError = systemImpl.getString(MR.strings.field_required_prompt))
                }
            }

            val validateUsername = entity.username?.let { validateUsernameUseCase(it) }

            if(validateUsername == null) {
                _uiState.update { prev ->
                    prev.copy(usernameError = systemImpl.getString(MR.strings.invalid))
                }
            }

            if(entity.newPassword != null && entity.newPassword.isBlank()) {
                _uiState.update { prev ->
                    prev.copy(newPasswordError = systemImpl.getString(MR.strings.field_required_prompt))
                }
            }

            if(_uiState.value.hasErrors) {
                loadingState = LoadingUiState.NOT_LOADING
                _uiState.update { prev ->
                    prev.copy(
                        fieldsEnabled = true
                    )
                }
                return@launch
            }

            if(entity.username != null && entity.newPassword != null) {
                //This is a registration
                try {
                    val usernameCount = activeRepo.personDao.countUsername(entity.username)
                    if(usernameCount == 0) {
                        activeRepo.withDoorTransactionAsync {
                            authManager.setAuth(entityUidArg, entity.newPassword)
                            val numChanges = activeRepo.personDao.updateUsername(
                                personUid = entityUidArg,
                                username = entity.username,
                                currentTime = systemTimeInMillis())

                            Napier.e("Updated username: $numChanges changes")
                        }

                        finishWithResult(null)
                    }else {
                        _uiState.update { prev ->
                            prev.copy(
                                usernameError = systemImpl.getString(MR.strings.person_exists)
                            )
                        }
                    }
                }catch(e: Exception) {
                    _uiState.update { prev ->
                        prev.copy(
                            usernameError = systemImpl.getString(MR.strings.login_network_error)
                        )
                    }
                }finally {
                    loadingState = LoadingUiState.NOT_LOADING
                    _uiState.update { prev ->
                        prev.copy(fieldsEnabled = true)
                    }
                }
            }

        }
    }

    companion object {
        const val DEST_NAME = "AccountEdit"
    }
}
