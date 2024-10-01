package com.ustadmobile.core.viewmodel.signup

import com.ustadmobile.core.MR
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.domain.localaccount.GetLocalAccountsSupportedUseCase
import com.ustadmobile.core.domain.passkey.CreatePasskeyParams
import com.ustadmobile.core.domain.passkey.CreatePasskeyUseCase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.appendSelectedAccount
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.core.viewmodel.person.child.AddChildProfilesViewModel
import com.ustadmobile.core.viewmodel.signup.SignUpViewModel.Companion.REGISTRATION_ARGS_TO_PASS
import com.ustadmobile.door.ext.doorIdentityHashCode
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonPicture
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.instanceOrNull


data class OtherSignUpOptionSelectionUiState(
    val showCreateLocaleAccount: Boolean = false,
    val person: Person? = null,
    val personPicture: PersonPicture? = null,
    val passkeySupported: Boolean = true,

    )

class OtherSignUpOptionSelectionViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destName: String = DEST_NAME
) : UstadEditViewModel(di, savedStateHandle, destName) {

    private val _uiState: MutableStateFlow<OtherSignUpOptionSelectionUiState> =
        MutableStateFlow(OtherSignUpOptionSelectionUiState())

    private val serverUrl = savedStateHandle[UstadView.ARG_LEARNINGSPACE_URL]?: "http://localhost"
    private val isParent = savedStateHandle[IS_PARENT].toBoolean()

    private val getLocalAccountsSupportedUseCase: GetLocalAccountsSupportedUseCase by instance()

    private val createPasskeyUseCase: CreatePasskeyUseCase? by instanceOrNull()

    private var nextDestination: String =
        savedStateHandle[UstadView.ARG_NEXT] ?: ClazzListViewModel.DEST_NAME_HOME

    val uiState: Flow<OtherSignUpOptionSelectionUiState> = _uiState.asStateFlow()


    init {
        viewModelScope.launch {
            val person = savedStateHandle.getJson(ARG_PERSON, Person.serializer()) ?: Person()
            val personPic = savedStateHandle.getJson(ARG_PERSON_PROFILE_PIC, PersonPicture.serializer()) ?: PersonPicture()
            _uiState.update { prev ->
                prev.copy(
                    person = person,
                    personPicture = personPic,
                    passkeySupported = createPasskeyUseCase != null,
                )
            }
        }

        loadingState = LoadingUiState.INDETERMINATE
        val title =
            systemImpl.getString(MR.strings.other_options)
        if (getLocalAccountsSupportedUseCase.invoke()) {
            _uiState.update { prev->
                prev.copy(
                    showCreateLocaleAccount = true
                )
            }
        }
        _appUiState.update {
            AppUiState(
                title = title,
                userAccountIconVisible = false,
                hideBottomNavigation = true,
                hideAppBar =false,
                navigationVisible = false,
            )
        }


    }

    fun onSignUpWithPasskey() {
        viewModelScope.launch {
            val uid = activeDb.doorPrimaryKeyManager.nextIdAsync(Person.TABLE_ID)
            val savePerson = _uiState.value.person ?: return@launch

            savePerson.personUid = uid

            val passkeyCreated = createPasskeyUseCase?.invoke(
                CreatePasskeyParams(
                    username = savePerson.firstNames.toString(),
                    personUid = uid.toString(),
                    doorNodeId = di.doorIdentityHashCode.toString(),
                    usStartTime = systemTimeInMillis(),
                    serverUrl = serverUrl,
                    person = savePerson
                )
            )
            passkeyCreated?.let {
                accountManager.registerWithPasskey(
                    serverUrl,
                    it,
                    savePerson,
                    _uiState.value.personPicture
                )
            }
            if (passkeyCreated == null) {
                snackDispatcher.showSnackBar(Snack(message = systemImpl.getString(MR.strings.sorry_something_went_wrong)))
                Napier.e { "Error occurred during creating passkey" }
                return@launch
            }
            if (isParent) {
                navController.navigate(
                    AddChildProfilesViewModel.DEST_NAME,
                    args = buildMap {
                        put(ARG_NEXT, nextDestination)
                        putFromSavedStateIfPresent(REGISTRATION_ARGS_TO_PASS)
                    }
                )

            } else {

                val goOptions = UstadMobileSystemCommon.UstadGoOptions(clearStack = true)
                Napier.d { "AddSignUpPresenter: go to next destination: $nextDestination" }
                navController.navigateToViewUri(
                    nextDestination.appendSelectedAccount(
                        savePerson.personUid,
                        LearningSpace(accountManager.activeLearningSpace.url)
                    ),
                    goOptions
                )

            }
        }

       // navController.popBackStack(SignUpViewModel.DEST_NAME,false)

    }
    fun onClickCreateLocalAccount() {
        loadingState = LoadingUiState.INDETERMINATE

        viewModelScope.launch {
            try {
                accountManager.createLocalAccount()
                val goOptions = UstadMobileSystemCommon.UstadGoOptions(clearStack = true)
                navController.navigate(ContentEntryListViewModel.DEST_NAME_HOME, emptyMap(), goOptions)
            } catch (e: Exception) {
                Napier.e("Error during login: ${e.message}", e)
            } finally {
                loadingState = LoadingUiState.NOT_LOADING
            }
        }
    }
    fun onclickSignUpWithUsernameAdPassword() {

        val args = buildMap {
            put(SignUpViewModel.SIGN_WITH_USERNAME_AND_PASSWORD, "true")
            putFromSavedStateIfPresent(REGISTRATION_ARGS_TO_PASS)
            put(
                ARG_PERSON,
                json.encodeToString( Person.serializer(),_uiState.value.person?:Person())
            )
            put(
                ARG_PERSON_PROFILE_PIC,
                json.encodeToString( PersonPicture.serializer(),_uiState.value.personPicture?:PersonPicture())
            )
            put(IS_PARENT,
                savedStateHandle[IS_PARENT].toString()
            )
        }

        navController.navigate(SignupEnterUsernamePasswordViewModel.DEST_NAME, args)


    }


    companion object {


        const val DEST_NAME = "otheroption"
        const val ARG_PERSON = "otheroptionperson"
        const val ARG_PERSON_PROFILE_PIC = "otheroptionpersonprofilepic"
        const val IS_PARENT = "isparent"




    }

}