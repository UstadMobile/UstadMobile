package com.ustadmobile.core.viewmodel.signup

import com.ustadmobile.core.MR
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase
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
import com.ustadmobile.core.util.ext.trimExcessWhiteSpace
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.core.viewmodel.person.child.AddChildProfileViewModel
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel
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
    val fullName: String? = null,

    val passwordConfirmedError: String? = null,

    val passwordError: String? = null,

    val emailError: String? = null,

    val confirmError: String? = null,

    val dateOfBirthError: String? = null,

    val parentContactError: String? = null,

    val genderError: String? = null,

    val firstNameError: String? = null,

    val lastNameError: String? = null,

    val isParent: Boolean = false,

    val isTeacher: Boolean = false,

    val signupWithPasskey: Boolean = true,

    val showCreatePasskeyPrompt: Boolean? = false,

    val doorNodeId: String? = null,

    val serverUrl_: String? = null
) {


}

class SignUpViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destName: String = DEST_NAME
) : UstadEditViewModel(di, savedStateHandle, destName) {

    private val _uiState: MutableStateFlow<SignUpUiState> = MutableStateFlow(SignUpUiState())

    private val createPasskeyUseCase: CreatePasskeyUseCase? by instanceOrNull()

    private var nextDestination: String =
        savedStateHandle[UstadView.ARG_NEXT] ?: ClazzListViewModel.DEST_NAME_HOME
    val uiState: Flow<SignUpUiState> = _uiState.asStateFlow()

    private val apiUrlConfig: SystemUrlConfig by instance()

    private val entityUid: Long
        get() = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0

    private val serverUrl = apiUrlConfig.newPersonalAccountsLearningSpaceUrl ?: "http://localhost"
    val addNewPersonUseCase: AddNewPersonUseCase = di.on(LearningSpace(serverUrl)).direct.instance()

    private val genderConfig: GenderConfig by instance()

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
        _uiState.update { prev ->
            prev.copy(
                genderOptions = genderConfig.genderMessageIdsAndUnset,
                showCreatePasskeyPrompt = false,
                person = Person(
                    dateOfBirth = savedStateHandle[PersonEditViewModel.ARG_DATE_OF_BIRTH]?.toLong()
                        ?: 0L
                ),
                serverUrl_ = serverUrl,
                signupWithPasskey = createPasskeyUseCase != null

            )
        }
        if (savedStateHandle[SIGN_WITH_USERNAME_AND_PASSWORD]== "true") {
            _uiState.update { prev ->
                prev.copy(

                    signupWithPasskey = false

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
                firstNameError = updateErrorMessageOnChange(
                    prev.person?.firstNames,
                    entity?.firstNames, prev.firstNameError
                ),
//                lastNameError = updateErrorMessageOnChange(
//                    prev.person?.lastName,
//                    entity?.lastName, prev.lastNameError
//                ),
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


    fun onParentCheckChanged(check: Boolean) {
        _uiState.update { prev ->
            prev.copy(isParent = check)
        }
    }

    fun onTeacherCheckChanged(check: Boolean) {
        _uiState.update { prev ->
            prev.copy(isTeacher = check)
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
                fullName = fullName
            )
        }

    }

    private fun SignUpUiState.hasErrors(): Boolean {
        return usernameError != null ||
                passwordError != null ||
                firstNameError != null ||
                //lastNameError != null ||
                genderError != null
    }

    private fun validateUsername(username: String): Boolean {
        var isValid = true

        if (username.isNullOrEmpty()) {
            isValid = false
        }

        if (isValid) {
            if (username.contains(" ")) {
                isValid = false
            }
        }

        if (isValid) {
            var usernameChars = username.toCharArray()

            for (i in 1..<usernameChars.count()) {
                if (usernameChars[i].isUpperCase()) {
                    isValid = false
                }
            }
        }

        return isValid
    }


    fun onSignUpWithPasskey() {


        loadingState = LoadingUiState.INDETERMINATE
        val fullName = _uiState.value.fullName?.trim()

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
                firstNameError = if (savePerson.firstNames.isNullOrEmpty()) requiredFieldMessage else null,
                //  lastNameError = if (savePerson.lastName.isNullOrEmpty()) requiredFieldMessage else null,
                genderError = if (savePerson.gender == GENDER_UNSET) requiredFieldMessage else null,
                usernameError = if (!_uiState.value.signupWithPasskey && !validateUsername(
                        savePerson.username ?: ""
                    )
                ) {
                    systemImpl.getString(MR.strings.invalid)
                } else {
                    null
                },
                passwordError = if (!_uiState.value.signupWithPasskey && savePerson.username.isNullOrBlank()) {
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
                if (_uiState.value.signupWithPasskey) {
                    val passkeyCreated = createPasskeyUseCase?.invoke(
                        CreatePasskeyParams
                            (
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

                if (_uiState.value.isTeacher) {
                    val goOptions = UstadMobileSystemCommon.UstadGoOptions(clearStack = true)
                    Napier.d { "AdSignUpdPresenter: go to next destination: $nextDestination" }
                    navController.navigateToViewUri(
                        nextDestination.appendSelectedAccount(
                            savePerson.personUid,
                            LearningSpace(accountManager.activeLearningSpace.url)
                        ),
                        goOptions
                    )
                }
                if (_uiState.value.isParent) {
                    navController.navigate(AddChildProfileViewModel.DEST_NAME, emptyMap())

                }
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

    fun onClickOtherOption() {

        navController.navigate(OtherSignUpOptionSelectionViewModel.DEST_NAME, emptyMap())

    }

    companion object {

        const val STATE_KEY_PICTURE = "picState"

        const val DEST_NAME = "SignUp"

        const val ARG_DATE_OF_BIRTH = "DateOfBirth"

        const val SIGN_WITH_USERNAME_AND_PASSWORD = "SignupWithUsernameAndPassowrd"


    }

}