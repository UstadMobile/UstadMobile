package com.ustadmobile.core.viewmodel.signup

import com.ustadmobile.core.MR
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.account.SendConsentRequestToParentUseCase
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase
import com.ustadmobile.core.domain.invite.EnrollToCourseFromInviteCodeUseCase
import com.ustadmobile.core.domain.localaccount.GetLocalAccountsSupportedUseCase
import com.ustadmobile.core.domain.credentials.CreatePasskeyUseCase
import com.ustadmobile.core.domain.filterusername.FilterUsernameUseCase
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.core.domain.validateemail.ValidateEmailUseCase
import com.ustadmobile.core.domain.username.GetUsernameSuggestionUseCase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
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
import com.ustadmobile.core.util.ext.putFromSavedStateIfPresent
import com.ustadmobile.core.util.ext.stringResourceOrMessage
import com.ustadmobile.core.view.SiteTermsDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.clazz.inviteredeem.ClazzInviteRedeemViewModel
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.core.viewmodel.person.child.ChildProfileListViewModel
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel.Companion.ARG_REGISTRATION_MODE
import com.ustadmobile.core.viewmodel.signup.OtherSignUpOptionSelectionViewModel.Companion.IS_PARENT
import com.ustadmobile.core.viewmodel.person.child.ChildProfileListViewModel.Companion.ARG_CHILD_DATE_OF_BIRTH
import com.ustadmobile.core.viewmodel.person.child.ChildProfileListViewModel.Companion.ARG_CHILD_GENDER
import com.ustadmobile.core.viewmodel.person.child.ChildProfileListViewModel.Companion.ARG_CHILD_NAME
import com.ustadmobile.core.viewmodel.person.child.ChildProfileListViewModel.Companion.ARG_PPJ_UID
import com.ustadmobile.core.viewmodel.person.registerminorwaitforparent.RegisterMinorWaitForParentViewModel
import com.ustadmobile.core.viewmodel.person.registerminorwaitforparent.RegisterMinorWaitForParentViewModel.Companion.ARG_REFERER_SCREEN
import com.ustadmobile.core.viewmodel.person.toFirstAndLastNameExt
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_UNSET
import com.ustadmobile.lib.db.entities.PersonParentJoin
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

    val parentEmail: String? = null,

    val dateOfBirthError: String? = null,

    val genderError: String? = null,

    val fullNameError: String? = null,

    val parentEmailError: String? = null,

    val isParent: Boolean = false,

    val isTeacher: Boolean = false,

    val passkeySupported: Boolean = true,

    val serverUrl_: String? = null,

    val showOtherOption: Boolean = true,

    val showPasskeyButton: Boolean = true,

    val isPersonalAccount: Boolean = false,

    val isMinor: Boolean = false,

    val isParentalConsentForMinor: Boolean = false,

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

    private var nextDestination: String =
        savedStateHandle[UstadView.ARG_NEXT] ?: ClazzListViewModel.DEST_NAME_HOME

    val uiState: Flow<SignUpUiState> = _uiState.asStateFlow()

    private val apiUrlConfig: SystemUrlConfig by instance()

    private val getLocalAccountsSupportedUseCase: GetLocalAccountsSupportedUseCase by instance()

    private val serverUrl = savedStateHandle[UstadView.ARG_LEARNINGSPACE_URL]
        ?: apiUrlConfig.newPersonalAccountsLearningSpaceUrl ?: "http://localhost"

    private val createPasskeyUseCase: CreatePasskeyUseCase? by di.on(LearningSpace(serverUrl)).instanceOrNull()

    val addNewPersonUseCase: AddNewPersonUseCase = di.on(LearningSpace(serverUrl)).direct.instance()

    private val validateEmailUseCase = ValidateEmailUseCase()

    val sendConsentRequestToParentUseCase: SendConsentRequestToParentUseCase =
        di.on(LearningSpace(serverUrl)).direct.instance()

    private val genderConfig: GenderConfig by instance()

    private val getUsernameSuggestionUseCase :GetUsernameSuggestionUseCase = di.on(LearningSpace(serverUrl)).direct.instance()

    private val filterUsernameUseCase = FilterUsernameUseCase()

    private var usernameSuggestionJob: Job? = null

    private val dateOfBirth: Long = savedStateHandle[ARG_DATE_OF_BIRTH]?.toLong()?:0L

    //Run EnqueueSavePictureUseCase after the database transaction has finished.
    private val enqueueSavePictureUseCase: EnqueueSavePictureUseCase by
        on(LearningSpace(serverUrl)).instance()

    private val enrollToCourseFromInviteCodeUseCase: EnrollToCourseFromInviteCodeUseCase =
        di.on(LearningSpace(serverUrl)).direct.instance()

    init {
        if (savedStateHandle[ARG_CHILD_NAME]!=null){
           _uiState.update { prev->
               prev.copy(
                   isParentalConsentForMinor = true,
                   isParent = true
               )
           }
        }

        _uiState.update { prev->
            prev.copy(
                isMinor = savedStateHandle[ARG_IS_MINOR].toBoolean()
            )
        }
        loadingState = LoadingUiState.INDETERMINATE

        val title = if (_uiState.value.isMinor) {
            systemImpl.getString(MR.strings.your_profile)
        } else {
            systemImpl.getString(MR.strings.create_account)
        }
        viewModelScope.launch {
            val person = savedStateHandle.getJson(
                OtherSignUpOptionSelectionViewModel.ARG_PERSON, Person.serializer(),
            ) ?: Person()
            val personPicture = savedStateHandle.getJson(
                OtherSignUpOptionSelectionViewModel.ARG_PERSON_PROFILE_PIC, PersonPicture.serializer(),
            )
            _uiState.update { prev ->
                prev.copy(
                    person = person,
                    personPicture=personPicture,
                    fullName = if (person.firstNames == "") {
                        null
                    }else{
                        person.fullName()
                    }
                )
            }
        }

        if (savedStateHandle[ARG_IS_PERSONAL_ACCOUNT] == "true") {
            _uiState.update { prev ->
                prev.copy(
                    isPersonalAccount = true
                )
            }
            nextDestination = ContentEntryListViewModel.DEST_NAME_HOME
        }
        _appUiState.update {
            AppUiState(
                title = title,
                hideAppBar =false,
                navigationVisible = false,
                userAccountIconVisible = false,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = (_uiState.value.isPersonalAccount&&_uiState.value.isMinor),
                    text = systemImpl.getString(MR.strings.next),
                    onClick = this::onClickDone
                )
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
                    dateOfBirth = dateOfBirth,
                    isPersonalAccount = _uiState.value.isPersonalAccount
                ),
                serverUrl_ = serverUrl,
                passkeySupported = createPasskeyUseCase != null,
                showOtherOption = createPasskeyUseCase == null &&
                        getLocalAccountsSupportedUseCase.invoke()&& !_uiState.value.isMinor,
                showPasskeyButton = !(_uiState.value.isMinor&&_uiState.value.isPersonalAccount)
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

    fun onClickDone(){

        val requiredFieldMessage = systemImpl.getString(MR.strings.field_required_prompt)
        val parentEmail = _uiState.value.parentEmail
        _uiState.update { prev ->
            prev.copy(
                fullNameError = if (_uiState.value.fullName.isNullOrEmpty()) requiredFieldMessage else null,
                parentEmailError = when{
                    parentEmail.isNullOrEmpty() -> requiredFieldMessage
                    parentEmail.let { validateEmailUseCase.invoke(it) } == null -> {
                        systemImpl.getString(MR.strings.invalid_email)
                    }
                    else -> null
                },
                genderError = if (_uiState.value.person?.gender == GENDER_UNSET) requiredFieldMessage else null,
            )
        }
        if (_uiState.value.hasErrorsIfBelow13()) {
            return
        }
        viewModelScope.launch {
            try {
                sendConsentAndNavigateToMinorWaitScreen(false)
            }catch(e: Throwable) {
                snackDispatcher.showSnackBar(Snack(e.message.toString()))
            }

        }

    }

    private suspend fun sendConsentAndNavigateToMinorWaitScreen(showUsernamePassword: Boolean) {
        val parentPersonParentJoin = PersonParentJoin().shallowCopy {
            ppjMinorPersonUid = _uiState.value.person?.personUid?:0L
        }
        val ppjUid = activeRepoWithFallback.personParentJoinDao().upsertAsync(parentPersonParentJoin)
        sendConsentRequestToParentUseCase(
            SendConsentRequestToParentUseCase.SendConsentRequestToParentRequest(
                childFullName = _uiState.value.fullName?:"",
                childDateOfBirth = dateOfBirth,
                childGender = _uiState.value.person?.gender?:0,
                parentContact = _uiState.value.parentEmail?:"",
                ppjUid = ppjUid
            )
        )
        val args = mutableMapOf<String, String>().also {
            it[RegisterMinorWaitForParentViewModel.ARG_USERNAME] = _uiState.value.person?.username ?: ""
            it[RegisterMinorWaitForParentViewModel.ARG_SHOW_USERNAME_PASSWORD] =
                showUsernamePassword.toString()
            it[RegisterMinorWaitForParentViewModel.ARG_PARENT_CONTACT] =
                _uiState.value.parentEmail?: ""
            it[RegisterMinorWaitForParentViewModel.ARG_PASSWORD] = _uiState.value.password ?: ""
            it[ARG_REFERER_SCREEN] = savedStateHandle[ARG_REFERER_SCREEN]?:""
            it.putFromSavedStateIfPresent(savedStateHandle, UstadView.ARG_POPUPTO_ON_FINISH)
        }
        navController.navigate(
            viewName = RegisterMinorWaitForParentViewModel.DEST_NAME,
            args = args,
            goOptions = UstadMobileSystemCommon.UstadGoOptions(clearStack = true)
        )
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


    fun onParentEmailValueChange(parentEmail: String) {
        _uiState.update { prev ->
            prev.copy(
               parentEmail = parentEmail
            )
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
    private fun SignUpUiState.hasErrorsIfBelow13(): Boolean {
        return fullNameError != null ||
                genderError != null ||
                parentEmailError != null
    }

    fun onClickSignup() {

        savedStateHandle[ARG_PARENT_CONTACT] = _uiState.value.parentEmail
        loadingState = LoadingUiState.INDETERMINATE

        // full name splitting into first name and last name
        val fullName = _uiState.value.fullName?.trim()
        val (firstName, lastName) = fullName.toFirstAndLastNameExt()

        onEntityChanged(
            _uiState.value.person?.shallowCopy {
                this.firstNames = firstName
                this.lastName = lastName
                dateOfBirth = savedStateHandle[ARG_DATE_OF_BIRTH]?.toLong()?:0L
                this.isPersonalAccount = _uiState.value.isPersonalAccount
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
                            personPicture = _uiState.value.personPicture,
                            isMinor = _uiState.value.isMinor
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
                    if (_uiState.value.isMinor){
                        sendConsentAndNavigateToMinorWaitScreen(false)
                        return@launch
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
            navController.navigate(ChildProfileListViewModel.DEST_NAME,
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
        val (firstName, lastName) = _uiState.value.fullName?.trim().toFirstAndLastNameExt()
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
        savedStateHandle[ARG_PARENT_CONTACT]=_uiState.value.parentEmail
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

        const val ARG_IS_MINOR = "isMinor"

        const val ARG_PARENT_CONTACT = "parentContact"


        val REGISTRATION_ARGS_TO_PASS = listOf(
            UstadView.ARG_LEARNINGSPACE_URL,
            SiteTermsDetailView.ARG_SHOW_ACCEPT_BUTTON,
            UstadView.ARG_POPUPTO_ON_FINISH,
            ARG_DATE_OF_BIRTH,
            ARG_REGISTRATION_MODE,
            ARG_NEW_OR_EXISTING_USER,
            ARG_IS_PERSONAL_ACCOUNT,
            ARG_IS_MINOR,
            ARG_CHILD_NAME,
            ARG_CHILD_GENDER,
            ARG_CHILD_DATE_OF_BIRTH,
            ARG_REFERER_SCREEN,
            ARG_PARENT_CONTACT,
            ARG_PPJ_UID
        )

        const val SIGN_WITH_USERNAME_AND_PASSWORD = "SignupWithUsernameAndPassword"


    }

}