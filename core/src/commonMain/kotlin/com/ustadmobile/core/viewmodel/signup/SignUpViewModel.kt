package com.ustadmobile.core.viewmodel.signup

import com.ustadmobile.core.MR
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.domain.ValidateUsername.ValidateUsernameUseCase
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase
import com.ustadmobile.core.domain.invite.EnrollToCourseFromInviteCodeUseCase
import com.ustadmobile.core.domain.localaccount.GetLocalAccountsSupportedUseCase
import com.ustadmobile.core.domain.credentials.CreatePasskeyUseCase
import com.ustadmobile.core.domain.filterusername.FilterUsernameUseCase
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.core.domain.username.GetUsernameSuggestionUseCase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.config.GenderConfig
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.impl.locale.entityconstants.PersonConstants
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.username.helper.UsernameErrorException
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.appendSelectedAccount
import com.ustadmobile.core.util.ext.stringResourceOrMessage
import com.ustadmobile.core.view.SiteTermsDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.clazz.inviteredeem.ClazzInviteRedeemViewModel
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.core.viewmodel.person.child.AddChildProfilesViewModel
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel.Companion.ARG_REGISTRATION_MODE
import com.ustadmobile.core.viewmodel.signup.OtherSignUpOptionSelectionViewModel.Companion.IS_PARENT
import com.ustadmobile.door.ext.doorIdentityHashCode
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_UNSET
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Job
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

    val fullName: String? = null,

    val dateOfBirthError: String? = null,

    val genderError: String? = null,

    val fullNameError: String? = null,

    val isParent: Boolean = false,

    val isTeacher: Boolean = false,

    val passkeySupported: Boolean = true,

    val serverUrl_: String? = null,

    val showOtherOption: Boolean = true,

    val isPersonalAccount: Boolean = false,

    val usernameError: String? = null,

    val usernameSetByUser: Boolean = false,

    val errorText: String? = null,

    ) {


}

class SignUpViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destName: String = DEST_NAME
) : UstadEditViewModel(di, savedStateHandle, destName) {

    private val _uiState: MutableStateFlow<SignUpUiState> = MutableStateFlow(SignUpUiState())

    private val validateUsernameUseCase: ValidateUsernameUseCase = ValidateUsernameUseCase()

    private var nextDestination: String =
        savedStateHandle[UstadView.ARG_NEXT] ?: ClazzListViewModel.DEST_NAME_HOME

    val uiState: Flow<SignUpUiState> = _uiState.asStateFlow()

    private val apiUrlConfig: SystemUrlConfig by instance()

    private val getLocalAccountsSupportedUseCase: GetLocalAccountsSupportedUseCase by instance()

    private val serverUrl = savedStateHandle[UstadView.ARG_LEARNINGSPACE_URL]
        ?: apiUrlConfig.newPersonalAccountsLearningSpaceUrl ?: "http://localhost"

    private val createPasskeyUseCase: CreatePasskeyUseCase? by di.on(LearningSpace(serverUrl)).instanceOrNull()

    val addNewPersonUseCase: AddNewPersonUseCase = di.on(LearningSpace(serverUrl)).direct.instance()

    private val genderConfig: GenderConfig by instance()

    private val getUsernameSuggestionUseCase :GetUsernameSuggestionUseCase = di.on(LearningSpace(serverUrl)).direct.instance()

    private val filterUsernameUseCase = FilterUsernameUseCase()

    private var usernameSuggestionJob: Job? = null

    //Run EnqueueSavePictureUseCase after the database transaction has finished.
    private val enqueueSavePictureUseCase: EnqueueSavePictureUseCase by
        on(LearningSpace(serverUrl)).instance()

    private val enrollToCourseFromInviteCodeUseCase: EnrollToCourseFromInviteCodeUseCase =
        di.on(LearningSpace(serverUrl)).direct.instance()

    init {
        loadingState = LoadingUiState.INDETERMINATE
        val title = systemImpl.getString(MR.strings.create_account)
        _appUiState.update {
            AppUiState(
                title = title,
                hideAppBar =false,
                navigationVisible = false,
                userAccountIconVisible = false,
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

        viewModelScope.launch {
            loadEntity(
                serializer = Person.serializer(),
                savedStateKey = OtherSignUpOptionSelectionViewModel.ARG_PERSON,
                onLoadFromDb = { null },
                makeDefault = {
                    Person()
                },
                uiUpdate = {
                    _uiState.update { prev ->
                        prev.copy(person = it, fullName = it?.fullName()?.trim() ?: "")
                    }
                }
            )

            loadEntity(
                serializer = PersonPicture.serializer(),
                savedStateKey = OtherSignUpOptionSelectionViewModel.ARG_PERSON_PROFILE_PIC,
                onLoadFromDb = { null },
                makeDefault = {
                    PersonPicture()
                },
                uiUpdate = {
                    _uiState.update { prev ->
                        prev.copy(personPicture = it)
                    }
                }
            )
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
    }

    fun onEntityChanged(entity: Person?) {
        _uiState.update { prev ->
            prev.copy(
                person = entity,
                genderError = updateErrorMessageOnChange(
                    prev.person?.gender,
                    entity?.gender, prev.genderError
                ),
                usernameError = updateErrorMessageOnChange(
                    prev.person?.username,
                    entity?.username, prev.usernameError
                ),
            )
        }

        scheduleEntityCommitToSavedState(
            entity = entity,
            serializer = Person.serializer(),
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


    fun onFullNameValueChange(fullName: String) {
        _uiState.update { prev ->
            prev.copy(
                fullName = fullName,
                fullNameError = updateErrorMessageOnChange(
                    prev.fullName,
                    fullName, prev.fullNameError
                ),
            )
        }
    }

    fun onUsernameChanged(newValue: String) {
        val filteredValue = filterUsernameUseCase(
            username = newValue,
            invalidCharReplacement = ""
        )

        val updatedPerson = _uiState.value.person?.shallowCopy {
            username = filteredValue
        }
        _uiState.update {
            it.copy(
                usernameSetByUser = _uiState.value.person?.username != filteredValue,
                usernameError = updateErrorMessageOnChange(
                    _uiState.value.person?.username,
                    updatedPerson?.username,
                    _uiState.value.usernameError
                ),
                person = updatedPerson,

            )
        }
    }

    fun onFullNameFocusedChanged(hasFocused: Boolean){
        if (hasFocused) return

        if (_uiState.value.usernameSetByUser&&!_uiState.value.person?.fullName().isNullOrEmpty())
            return

        usernameSuggestionJob?.cancel()

        usernameSuggestionJob = viewModelScope.launch {

            val fullName = _uiState.value.fullName.orEmpty()
            if (fullName.isBlank()) return@launch

            try {

                val suggestedUsername = getUsernameSuggestionUseCase(fullName)

                val updatedPerson = _uiState.value.person?.shallowCopy {
                    username = suggestedUsername
                }

                _uiState.update {
                    it.copy(
                        person = updatedPerson,
                        usernameError = updateErrorMessageOnChange(
                            _uiState.value.person?.username,
                            updatedPerson?.username,
                            _uiState.value.usernameError
                        ),
                    )
                }

            } catch (e: UsernameErrorException) {
                _uiState.update { prev ->
                    prev.copy(
                        usernameError = e.stringResourceOrMessage(systemImpl),
                    )
                }
            }catch (e: Exception) {
                _uiState.update { prev ->
                    prev.copy(
                        errorText = e.stringResourceOrMessage(systemImpl),
                    )
                }
            }
        }

    }


    private fun SignUpUiState.hasErrors(): Boolean {
        return fullNameError != null || genderError != null|| usernameError != null
    }


    fun onClickSignup() {
        loadingState = LoadingUiState.INDETERMINATE

        // full name splitting into first name and last name
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
                fullNameError = if (savePerson.firstNames.isNullOrEmpty())
                    requiredFieldMessage
                else
                    null,
                genderError = if (savePerson.gender == GENDER_UNSET)
                    requiredFieldMessage
                else
                    null,
                usernameError = if (savePerson.username.isNullOrEmpty())
                    requiredFieldMessage
                else
                    null,
            )
        }

        if (_uiState.value.hasErrors()) {
            loadingState = LoadingUiState.NOT_LOADING
            return
        }

        val createPasskeyUseCaseVal = createPasskeyUseCase

        viewModelScope.launch {
            try {
                val uid = activeDb.doorPrimaryKeyManager.nextIdAsync(Person.TABLE_ID)
                savePerson.personUid = uid

                if(createPasskeyUseCaseVal != null) {
                    try {
                        val username = savePerson.username ?: throw
                        IllegalStateException("username can not be null")

                        val passkeyCreated = createPasskeyUseCaseVal(
                            username = username,
                        )

                        accountManager.registerWithPasskey(
                            learningSpaceUrl = serverUrl,
                            passkeyResult = passkeyCreated,
                            person = savePerson,
                            personPicture = _uiState.value.personPicture
                        )


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

                        enrollToCourseFromInviteUid(savePerson.personUid)
                        navigateToAppropriateScreen(savePerson)
                    }catch (e:Exception){
                        _uiState.update { prev ->
                            prev.copy(
                                errorText = e.stringResourceOrMessage(systemImpl),
                            )
                        }
                    }

                } else {
                    navController.navigate(SignupEnterUsernamePasswordViewModel.DEST_NAME,
                        args = buildMap {
                            putAllFromSavedStateIfPresent(REGISTRATION_ARGS_TO_PASS)
                            putFromSavedStateIfPresent(ARG_NEXT)
                            put(
                                OtherSignUpOptionSelectionViewModel.ARG_PERSON,
                                json.encodeToString( Person.serializer(),savePerson)
                            )
                            put(
                                OtherSignUpOptionSelectionViewModel.ARG_PERSON_PROFILE_PIC,
                                json.encodeToString( PersonPicture.serializer(),_uiState.value.personPicture?: PersonPicture())
                            )

                            put(IS_PARENT,
                                _uiState.value.isParent.toString()
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                Napier.d { "exception ${e.message}" }
                snackDispatcher.showSnackBar(
                    Snack(systemImpl.getString(MR.strings.error) + ":" + e.message)
                )
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
                    put(ARG_NEXT, nextDestination)
                    putAllFromSavedStateIfPresent(REGISTRATION_ARGS_TO_PASS)
                    putFromSavedStateIfPresent(ARG_NEXT)
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
        // full name splitting into first name and last name
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
                fullNameError = if (savePerson.firstNames.isNullOrEmpty()) requiredFieldMessage else null,
                genderError = if (savePerson.gender == GENDER_UNSET) requiredFieldMessage else null,
                usernameError = if (savePerson.username.isNullOrEmpty()) requiredFieldMessage else null,

                )
        }
        if (_uiState.value.hasErrors()) {
            return
        }
        navController.navigate(OtherSignUpOptionSelectionViewModel.DEST_NAME,
            args = buildMap {
                putAllFromSavedStateIfPresent(REGISTRATION_ARGS_TO_PASS)
                putFromSavedStateIfPresent(ARG_NEXT)
                put(
                    OtherSignUpOptionSelectionViewModel.ARG_PERSON,
                    json.encodeToString( Person.serializer(),savePerson)
                )
                put(
                    OtherSignUpOptionSelectionViewModel.ARG_PERSON_PROFILE_PIC,
                    json.encodeToString( PersonPicture.serializer(),_uiState.value.personPicture?: PersonPicture())
                )

                put(IS_PARENT,
                    _uiState.value.isParent.toString()
                )
            }
        )

    }

    private suspend fun enrollToCourseFromInviteUid(personUid: Long) {
        val viewUri= savedStateHandle[UstadView.ARG_NEXT]
        if (viewUri != null && viewUri.contains(ClazzInviteRedeemViewModel.DEST_NAME)) {
            nextDestination = ClazzListViewModel.DEST_NAME_HOME
            enrollToCourseFromInviteCodeUseCase(
                viewUri = viewUri,
                personUid = personUid
            )
        }
    }

    companion object {

        const val STATE_KEY_PICTURE = "picState"

        const val DEST_NAME = "SignUp"

        const val ARG_DATE_OF_BIRTH = "DateOfBirth"

        const val ARG_IS_PERSONAL_ACCOUNT = "personalAccount"

        const val ARG_NEW_OR_EXISTING_USER = "newOrExisting"

        const val ARG_VAL_NEW_USER = "new"

        const val ARG_VAL_EXISTING_USER = "existing"

        val REGISTRATION_ARGS_TO_PASS = listOf(
            UstadView.ARG_LEARNINGSPACE_URL,
            SiteTermsDetailView.ARG_SHOW_ACCEPT_BUTTON,
            UstadView.ARG_POPUPTO_ON_FINISH,
            ARG_DATE_OF_BIRTH,
            ARG_REGISTRATION_MODE,
            ARG_NEW_OR_EXISTING_USER,
            ARG_IS_PERSONAL_ACCOUNT
        )

        const val SIGN_WITH_USERNAME_AND_PASSWORD = "SignupWithUsernameAndPassword"


    }

}