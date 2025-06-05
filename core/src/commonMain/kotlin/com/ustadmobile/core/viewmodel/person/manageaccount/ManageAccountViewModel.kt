package com.ustadmobile.core.viewmodel.person.manageaccount

import com.ustadmobile.core.MR
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.db.UmAppDataLayer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.credentials.CreatePasskeyUseCase
import com.ustadmobile.core.domain.credentials.SavePersonPasskeyUseCase
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.person.accountedit.PersonAccountEditViewModel
import com.ustadmobile.core.viewmodel.person.passkey.PasskeyListViewModel
import com.ustadmobile.core.viewmodel.signup.SignUpViewModel
import com.ustadmobile.door.ext.doorIdentityHashCode
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.PersonAuth2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import org.kodein.di.on

data class ManageAccountUiState(
    val passkeyCount: Int = 0,
    val showCreatePasskey: Boolean = false,
    val passkeySupported: Boolean = true,
    val personName: String = "",
    val personUsername: String = "",
    val personAuth: PersonAuth2 ? = null
)

class ManageAccountViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,

    ) : UstadViewModel(di, savedStateHandle, DEST_NAME) {


    private val personUid = savedStateHandle[ARG_ENTITY_UID]?.toLong() ?: 0

    private val personName = savedStateHandle[PERSON_FULL_NAME] ?: ""

    private val personUsername = savedStateHandle[PERSON_USERNAME] ?: ""

    private val apiUrlConfig: SystemUrlConfig by instance()

    private val _uiState = MutableStateFlow(
        ManageAccountUiState()
    )

    private val createPasskeyUseCase: CreatePasskeyUseCase? by instanceOrNull()

    val uiState: Flow<ManageAccountUiState> = _uiState.asStateFlow()

    private val savePassKeyUseCase: SavePersonPasskeyUseCase?=
        di.on(LearningSpace(apiUrlConfig.systemBaseUrl)).direct.instanceOrNull()

    init {
        viewModelScope.launch {
            activeDataLayer.repositoryOrLocalDb.personAuth2Dao().findByPersonUidFlow(personUid).collect{
                _uiState.update { prev ->
                    prev.copy(
                        personAuth = it
                    )
                }
            }
        }

        _appUiState.value = AppUiState(
            userAccountIconVisible = false,
            navigationVisible = false,
            title = if (savedStateHandle[UstadListViewModel.ARG_LISTMODE] == ListViewMode.PICKER.mode) {
                systemImpl.getString(MR.strings.select_account)
            } else {
                systemImpl.getString(MR.strings.accounts)
            }
        )
        _uiState.update { prev ->
            prev.copy(
                personUsername = personUsername,
                personName = personName,
                passkeySupported = (createPasskeyUseCase != null &&
                        accountManager.currentAccount.personUid==personUid) ,
            )
        }

        viewModelScope.launch {
            val repo: UmAppDatabase =
                di.on(LearningSpace(apiUrlConfig.systemBaseUrl)).direct.instance<UmAppDataLayer>()
                    .requireRepository()
            val activePasskeys = repo.personPasskeyDao().getAllActivePasskeys(accountManager.currentAccount.personUid)
            activePasskeys.collect {
                _uiState.update { prev ->
                    prev.copy(
                        showCreatePasskey = it.isEmpty(),
                        passkeyCount = it.size
                    )
                }
            }
        }
    }

    fun navigateToEditAccount() {
        navController.navigate(
            PersonAccountEditViewModel.DEST_NAME,
            mapOf(ARG_ENTITY_UID to personUid.toString()))
    }

    fun onClickManagePasskey() {
        navController.navigate(
            PasskeyListViewModel.DEST_NAME,
            args = buildMap {
                putAllFromSavedStateIfPresent(SignUpViewModel.REGISTRATION_ARGS_TO_PASS)
            }
        )
    }

    fun onCreatePasskeyClick() {
        viewModelScope.launch {
            val passkeyCreated = createPasskeyUseCase?.invoke(
                username = accountManager.currentUserSession.person.username.toString(),
            )
            if (passkeyCreated != null) {
                when (passkeyCreated) {
                    is CreatePasskeyUseCase.CreatePasskeyResult -> {
                        savePassKeyUseCase?.invoke(
                            passkeyResult = passkeyCreated.authenticationResponseJSON,
                            person = accountManager.currentUserSession.person
                        )
                    }

                    is CreatePasskeyUseCase.Error -> {
                        snackDispatcher.showSnackBar(Snack(message = passkeyCreated.message.toString()))


                    }

                    is CreatePasskeyUseCase.UserCanceledResult -> {
                        //do nothing
                    }

                    null -> {
                        //do nothing
                    }


                }

            }

        }
    }

    companion object {

        const val DEST_NAME = "ManageAccount"
        const val PERSON_FULL_NAME = "PersonFullName"
        const val PERSON_USERNAME = "PersonUsername"


    }
}