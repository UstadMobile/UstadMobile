package com.ustadmobile.core.viewmodel.person.accountedit

import com.ustadmobile.core.MR
import com.ustadmobile.core.account.AuthManager
import com.ustadmobile.core.account.UnauthorizedException
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.domain.account.SetPasswordUseCase
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.person.accountedit.PersonAccountEditViewModel.Companion.MODE_CHANGE_PASS
import com.ustadmobile.core.viewmodel.person.accountedit.PersonAccountEditViewModel.Companion.MODE_CREATE_ACCOUNT
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
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

    val mode: Int = MODE_CREATE_ACCOUNT,

    val personUid: Long = 0,

    val username: String = "",

    val currentPassword: String = "",

    val newPassword: String = "",

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
        get() =  personAccount?.mode == MODE_CREATE_ACCOUNT

    val currentPasswordVisible: Boolean

        get() = personAccount?.mode == MODE_CHANGE_PASS

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

    private val setPasswordUseCase: SetPasswordUseCase by on(accountManager.activeEndpoint).instance()

    init {
        _appUiState.value = AppUiState(
            loadingState = LoadingUiState.INDETERMINATE,
        )

        launchIfHasPermission(
            permissionCheck = { db ->
                if(entityUidArg == activeUserPersonUid) {
                    true
                }else {
                    db.systemPermissionDao().personHasSystemPermission(
                        activeUserPersonUid, PermissionFlags.EDIT_ALL_PERSONS
                    )
                }
            }
        ) {
            loadEntity(
                serializer = PersonUsernameAndPasswordModel.serializer(),
                onLoadFromDb = { db ->
                    val person = db.personDao().findByUidAsync(entityUidArg)
                    val hasResetPermission = db.systemPermissionDao().personHasSystemPermission(
                        accountPersonUid = activeUserPersonUid,
                        permission = PermissionFlags.RESET_PASSWORDS,
                    )


                    if(person == null) {
                        null
                    }else {
                        PersonUsernameAndPasswordModel(
                            mode = when {
                                person.username == null -> MODE_CREATE_ACCOUNT
                                hasResetPermission -> MODE_RESET
                                else -> MODE_CHANGE_PASS
                            },
                            personUid = person.personUid,
                            username = person.username ?: "",
                            currentPassword = "",
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
            if(entity.mode == MODE_CREATE_ACCOUNT && entity.username.isBlank()) {
                _uiState.update { prev ->
                    prev.copy(usernameError = systemImpl.getString(MR.strings.field_required_prompt))
                }
            }

            if(entity.newPassword.isBlank()) {
                _uiState.update { prev ->
                    prev.copy(newPasswordError = systemImpl.getString(MR.strings.field_required_prompt))
                }
            }

            if(entity.mode == MODE_CHANGE_PASS && entity.currentPassword.isBlank())

            if(_uiState.value.hasErrors) {
                loadingState = LoadingUiState.NOT_LOADING
                _uiState.update { prev ->
                    prev.copy(
                        fieldsEnabled = true
                    )
                }
                return@launch
            }

            if(entity.mode == MODE_CREATE_ACCOUNT) {
                //This is a registration
                try {
                    val usernameCount = activeRepoWithFallback.personDao().countUsername(entity.username)
                    if(usernameCount == 0) {
                        activeRepoWithFallback.withDoorTransactionAsync {
                            authManager.setAuth(entityUidArg, entity.newPassword)
                            val numChanges = activeRepoWithFallback.personDao().updateUsername(
                                personUid = entityUidArg,
                                username = entity.username,
                                currentTime = systemTimeInMillis()
                            )

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
            }else {
                try {
                    setPasswordUseCase(
                        activeUserPersonUid = activeUserPersonUid,
                        personUid = entity.personUid,
                        username = entity.username,
                        newPassword = entity.newPassword,
                        currentPassword = if(entity.mode == MODE_CHANGE_PASS)
                            entity.currentPassword
                        else
                            null
                    )

                    snackDispatcher.showSnackBar(Snack(systemImpl.getString(MR.strings.password_updated)))
                    finishWithResult(null)
                }catch(e: Exception) {
                    if(e is UnauthorizedException) {
                        _uiState.update { prev ->
                            prev.copy(
                                currentPasswordError = systemImpl.getString(MR.strings.wrong_user_pass_combo),
                            )
                        }
                    }else {
                        _uiState.update { prev ->
                            prev.copy(
                                errorMessage = e.message
                            )
                        }
                    }
                }finally {
                    loadingState = LoadingUiState.NOT_LOADING
                    _uiState.update { prev -> prev.copy(fieldsEnabled = true) }
                }
            }
        }
    }

    companion object {
        const val DEST_NAME = "AccountEdit"

        const val MODE_CREATE_ACCOUNT = 1

        const val MODE_RESET = 2

        const val MODE_CHANGE_PASS = 3
    }
}
