package com.ustadmobile.core.viewmodel.signup

import com.ustadmobile.core.MR
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.domain.ValidateUsername.ValidateUsernameUseCase
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase
import com.ustadmobile.core.domain.localaccount.GetLocalAccountsSupportedUseCase
import com.ustadmobile.core.domain.passkey.CreatePasskeyParams
import com.ustadmobile.core.domain.passkey.CreatePasskeyUseCase
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.config.GenderConfig
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.impl.locale.entityconstants.PersonConstants
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.appendSelectedAccount
import com.ustadmobile.core.view.SiteTermsDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.core.viewmodel.person.child.AddChildProfilesViewModel
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel.Companion.ARG_REGISTRATION_MODE
import com.ustadmobile.door.ext.doorIdentityHashCode
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_UNSET
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import io.github.aakira.napier.Napier
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


data class SignUpUiState(

    val person: Person? = null,

    val password: String? = null,

    val genderOptions: List<MessageIdOption2> = PersonConstants.GENDER_MESSAGE_IDS_AND_UNSET,

    val personPicture: PersonPicture? = null,

    val registrationMode: Int = 0,

    val usernameError: String? = null,

    val firstName: String? = null,

    val passwordError: String? = null,

    val dateOfBirthError: String? = null,

    val parentContactError: String? = null,

    val genderError: String? = null,

    val fullNameError: String? = null,

    val isParent: Boolean = false,

    val isTeacher: Boolean = false,

    val passkeySupported: Boolean = true,

    val doorNodeId: String? = null,

    val serverUrl_: String? = null,

    val showOtherOption: Boolean = true,

    val isPersonalAccount: Boolean = false,
) {


}

class SignUpViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destName: String = DEST_NAME
) : UstadEditViewModel(di, savedStateHandle, destName) {

    private val _uiState: MutableStateFlow<SignUpUiState> = MutableStateFlow(SignUpUiState())

    private val createPasskeyUseCase: CreatePasskeyUseCase? by instanceOrNull()

    private val validateUsernameUseCase: ValidateUsernameUseCase = ValidateUsernameUseCase()

    private var nextDestination: String =
        savedStateHandle[UstadView.ARG_NEXT] ?: ClazzListViewModel.DEST_NAME_HOME

    val uiState: Flow<SignUpUiState> = _uiState.asStateFlow()

    private val apiUrlConfig: SystemUrlConfig by instance()

    private val getLocalAccountsSupportedUseCase: GetLocalAccountsSupportedUseCase by instance()

    private val serverUrl = savedStateHandle[UstadView.ARG_LEARNINGSPACE_URL]
        ?: apiUrlConfig.newPersonalAccountsLearningSpaceUrl ?: "http://localhost"

    val addNewPersonUseCase: AddNewPersonUseCase = di.on(LearningSpace(serverUrl)).direct.instance()

    private val genderConfig: GenderConfig by instance()

    //Run EnqueueSavePictureUseCase after the database transaction has finished.
    private val enqueueSavePictureUseCase: EnqueueSavePictureUseCase by
    on(LearningSpace(serverUrl)).instance()


    init {
        loadingState = LoadingUiState.INDETERMINATE
        val title =
            systemImpl.getString(MR.strings.create_account)

        _appUiState.update {
            AppUiState(
                title = title,
                userAccountIconVisible = false,
                hideBottomNavigation = true,
            )
        }
        if (savedStateHandle[ARG_IS_PERSONAL_ACCOUNT] == "true") {
            _uiState.update { prev ->
                prev.copy(
                    isPersonalAccount = true
                )
            }
            nextDestination = ContentEntryListViewModel.DEST_NAME_HOME
        }
        _uiState.update { prev ->
            prev.copy(
                genderOptions = genderConfig.genderMessageIdsAndUnset,
                person = Person(
                    dateOfBirth = savedStateHandle[PersonEditViewModel.ARG_DATE_OF_BIRTH]?.toLong()
                        ?: 0L,
                    isPersonalAccount = _uiState.value.isPersonalAccount
                ),
                serverUrl_ = serverUrl,
                passkeySupported = createPasskeyUseCase != null,
                showOtherOption = createPasskeyUseCase == null && getLocalAccountsSupportedUseCase.invoke(),

                )
        }
        if (savedStateHandle[SIGN_WITH_USERNAME_AND_PASSWORD] == "true") {
            _uiState.update { prev ->
                prev.copy(
                    passkeySupported = false
                )
            }
        }
    }

    fun onEntityChanged(entity: Person?) {
        _uiState.update { prev ->
            prev.copy(
                person = entity,
                genderError = updateErrorMessageOnChange(
                    prev.person?.gender,
                    entity?.gender, prev.genderError
                ),
                fullNameError = updateErrorMessageOnChange(
                    prev.person?.firstNames,
                    entity?.firstNames, prev.fullNameError
                ),
                usernameError = updateErrorMessageOnChange(
                    prev.person?.username,
                    entity?.username, prev.usernameError
                ),
            )
        }

        scheduleEntityCommitToSavedState(
            entity, serializer = Person.serializer(),
            commitDelay = 200
        )
    }


    fun onParentCheckChanged(checked: Boolean) {
        _uiState.update { prev ->
            prev.copy(isParent = checked)
        }
    }

    fun onTeacherCheckChanged(checked: Boolean) {
        _uiState.update { prev ->
            prev.copy(isTeacher = checked)
        }
    }

    fun onPersonPictureChanged(pictureUri: String?) {
        val personPicture: PersonPicture = PersonPicture().apply {
            personPictureUid = _uiState.value.personPicture?.personPictureUid ?: 0
            personPictureUri = pictureUri
        }

        _uiState.update { prev ->
            prev.copy(
                personPicture = personPicture
            )
        }

        viewModelScope.launch {
            savedStateHandle.setJson(STATE_KEY_PICTURE, PersonPicture.serializer(), personPicture)
        }
    }

    fun onPasswordChanged(password: String?) {
        _uiState.update { prev ->
            prev.copy(
                password = password,
                passwordError = null,
            )
        }
    }

    fun onFullNameValueChange(fullName: String) {
        _uiState.update { prev ->
            prev.copy(
                firstName = fullName
            )
        }

    }

    private fun SignUpUiState.hasErrors(): Boolean {
        return usernameError != null ||
                passwordError != null ||
                fullNameError != null ||
                genderError != null
    }


    fun onClickedSignup() {


        loadingState = LoadingUiState.INDETERMINATE

        // full name splitting into first name and last name
        val fullName = _uiState.value.firstName?.trim()
        val parts = fullName?.trim()?.split(" ", limit = 2)
        val firstName = parts?.get(0)
        val lastName = parts?.getOrElse(1) { "" }
        onEntityChanged(
            _uiState.value.person?.shallowCopy {
                this.firstNames = firstName
                this.lastName = lastName
            }
        )

        val savePerson = _uiState.value.person ?: return

        val requiredFieldMessage = systemImpl.getString(MR.strings.field_required_prompt)

        _uiState.update { prev ->
            prev.copy(
                fullNameError = if (savePerson.firstNames.isNullOrEmpty()) requiredFieldMessage else null,
                genderError = if (savePerson.gender == GENDER_UNSET) requiredFieldMessage else null,
                usernameError = if (!_uiState.value.passkeySupported && !validateUsernameUseCase.invoke(
                        savePerson.username ?: ""
                    )
                ) {
                    systemImpl.getString(MR.strings.invalid)
                } else {
                    null
                },
                passwordError = if (!_uiState.value.passkeySupported && savePerson.username.isNullOrBlank()) {
                    systemImpl.getString(MR.strings.field_required_prompt)
                } else {
                    null
                }
            )
        }

        if (_uiState.value.hasErrors()) {
            loadingState = LoadingUiState.NOT_LOADING
            return
        }

        viewModelScope.launch {


            try {
                val uid = activeDb.doorPrimaryKeyManager.nextIdAsync(Person.TABLE_ID)
                savePerson.personUid = uid

                if (_uiState.value.passkeySupported) {
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
                } else {
                    val passwordVal = _uiState.value.password ?: return@launch
                    accountManager.register(
                        person = savePerson,
                        password = passwordVal,
                        learningSpaceUrl = serverUrl
                    )
                }


                val personPictureVal = _uiState.value.personPicture
                if (personPictureVal != null) {
                    personPictureVal.personPictureUid = savePerson.personUid
                    personPictureVal.personPictureLct = systemTimeInMillis()
                    val personPictureUriVal = personPictureVal.personPictureUri

                    enqueueSavePictureUseCase(
                        entityUid = savePerson.personUid,
                        tableId = PersonPicture.TABLE_ID,
                        pictureUri = personPictureUriVal
                    )

                }

                navigateToAppropriateScreen(savePerson)

            } catch (e: Exception) {
                if (e is IllegalStateException) {
                    _uiState.update { prev ->
                        prev.copy(usernameError = systemImpl.getString(MR.strings.person_exists))
                    }
                } else {
                    snackDispatcher.showSnackBar(
                        Snack(systemImpl.getString(MR.strings.login_network_error))
                    )
                }
                return@launch
            } finally {
                loadingState = LoadingUiState.NOT_LOADING
            }
        }
    }

    private fun navigateToAppropriateScreen(savePerson: Person) {

        if (_uiState.value.isParent) {
            navController.navigate(AddChildProfilesViewModel.DEST_NAME,
                args = buildMap {
                    put(ARG_NEXT,nextDestination)
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

    fun onClickOtherOption() {

        navController.navigate(OtherSignUpOptionSelectionViewModel.DEST_NAME,
            args = buildMap {
                putFromSavedStateIfPresent(REGISTRATION_ARGS_TO_PASS)
            }
        )

    }

    companion object {

        const val STATE_KEY_PICTURE = "picState"

        const val DEST_NAME = "SignUp"

        const val ARG_DATE_OF_BIRTH = "DateOfBirth"


        const val ARG_IS_PERSONAL_ACCOUNT = "personalAccount"

        const val ARG_NEW_OR_EXISTING_USER = "NewOrExistingUser"

        val REGISTRATION_ARGS_TO_PASS = listOf(
            UstadView.ARG_LEARNINGSPACE_URL,
            SiteTermsDetailView.ARG_SHOW_ACCEPT_BUTTON,
            UstadView.ARG_POPUPTO_ON_FINISH,
            ARG_DATE_OF_BIRTH,
            ARG_REGISTRATION_MODE,
            ARG_NEW_OR_EXISTING_USER,
            ARG_IS_PERSONAL_ACCOUNT
        )

        const val SIGN_WITH_USERNAME_AND_PASSWORD = "SignupWithUsernameAndPassowrd"


    }

}