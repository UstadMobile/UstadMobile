package com.ustadmobile.core.viewmodel.person.edit

import com.ustadmobile.core.account.AccountRegisterOptions
import com.ustadmobile.core.MR
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.core.domain.phonenumber.PhoneNumValidatorUseCase
import com.ustadmobile.core.domain.validateemail.ValidateEmailUseCase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.config.ApiUrlConfig
import com.ustadmobile.core.impl.config.GenderConfig
import com.ustadmobile.core.impl.locale.entityconstants.PersonConstants
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.core.view.*
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.person.PersonViewModelConstants.ARG_GO_TO_ON_PERSON_SELECTED
import com.ustadmobile.core.viewmodel.person.detail.PersonDetailViewModel
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel.Companion.REGISTER_MODE_ENABLED
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel.Companion.REGISTER_MODE_MINOR
import com.ustadmobile.core.viewmodel.person.registerageredirect.RegisterAgeRedirectViewModel
import com.ustadmobile.core.viewmodel.person.registerminorwaitforparent.RegisterMinorWaitForParentViewModel
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_UNSET
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

data class PersonEditUiState(

    val person: Person? = null,

    val password: String? = null,

    val genderOptions: List<MessageIdOption2> = PersonConstants.GENDER_MESSAGE_IDS_AND_UNSET,

    val personPicture: PersonPicture? = null,

    val fieldsEnabled: Boolean = false,

    /**
     * This is set only when registering a minor
     */
    val approvalPersonParentJoin: PersonParentJoin? = null,

    val registrationMode: Int = 0,

    val usernameError: String? = null,

    val passwordConfirmedError: String? = null,

    val passwordError: String? = null,

    val emailError: String? = null,

    val confirmError: String? = null,

    val dateOfBirthError: String? = null,

    val parentContactError: String? = null,

    val genderError: String? = null,

    val firstNameError: String? = null,

    val lastNameError: String? = null,

    val phoneNumError: String? = null,

    /**
     * Used to determine if the user has actually set a phone number. This is set by the UI
     * components as a user inputs a number. True if the national phone number part (e.g. not just
     * country code) is set, false otherwise.
     *
     * A person without any phone number set is allowed, but if a number is entered, it will be
     * validated.
     */
    val nationalPhoneNumSet: Boolean = false,

) {

    val parentalEmailVisible: Boolean
        get() = approvalPersonParentJoin != null


    val dateOfBirthVisible: Boolean
        get() = !registrationMode.hasFlag(REGISTER_MODE_ENABLED)

    val usernameVisible: Boolean
        get() = registrationMode.hasFlag(REGISTER_MODE_ENABLED)

    val passwordVisible: Boolean
        get() = registrationMode.hasFlag(REGISTER_MODE_ENABLED)

    val emailVisible: Boolean
        get() = !registrationMode.hasFlag(REGISTER_MODE_MINOR)

    val phoneNumVisible: Boolean
        get() = !registrationMode.hasFlag(REGISTER_MODE_MINOR)

    val personAddressVisible: Boolean
        get() = !registrationMode.hasFlag(REGISTER_MODE_MINOR)

}

class PersonEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destName: String = DEST_NAME
): UstadEditViewModel(di, savedStateHandle, destName) {

    private val _uiState: MutableStateFlow<PersonEditUiState> = MutableStateFlow(PersonEditUiState())

    val uiState: Flow<PersonEditUiState> = _uiState.asStateFlow()

    private val registrationModeFlags = savedStateHandle[ARG_REGISTRATION_MODE]?.toInt()
        ?: REGISTER_MODE_NONE

    private val apiUrlConfig: ApiUrlConfig by instance()

    private val entityUid: Long
        get() = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0

    private val serverUrl = savedStateHandle[UstadView.ARG_API_URL]
        ?: apiUrlConfig.presetApiUrl ?: "http://localhost"

    private val nextDestination = savedStateHandle[UstadView.ARG_NEXT] ?: systemImpl.getDefaultFirstDest()

    private val phoneNumValidatorUseCase: PhoneNumValidatorUseCase by instance()

    private val validateEmailUseCase = ValidateEmailUseCase()

    private val genderConfig : GenderConfig by instance()

    private val enqueueSavePictureUseCase: EnqueueSavePictureUseCase by
        on(accountManager.activeEndpoint).instance()

    private val addNewPersonUseCase: AddNewPersonUseCase by di.onActiveEndpoint().instance()

    private val dontSetCurrentSession: Boolean = savedStateHandle[ARG_DONT_SET_CURRENT_SESSION]
        ?.toBoolean() ?: false

    init {
        loadingState = LoadingUiState.INDETERMINATE


        val title = if(registrationModeFlags.hasFlag(REGISTER_MODE_ENABLED)) {
            systemImpl.getString(MR.strings.my_profile)
        }else {
            if(entityUid == 0L)
                systemImpl.getString(MR.strings.add_a_new_person)
            else
                systemImpl.getString(MR.strings.edit_person)
        }
        _appUiState.update {
            AppUiState(
                title = title,
                hideBottomNavigation = true,
                navigationVisible = !registrationModeFlags.hasFlag(REGISTER_MODE_ENABLED),
            )
        }
        _uiState.update { prev ->
            prev.copy(
                genderOptions = genderConfig.genderMessageIdsAndUnset,
                registrationMode = registrationModeFlags
            )
        }

        launchIfHasPermission(
            permissionCheck = { db ->
                when {
                    //Viewing in register mode is allowed
                    registrationModeFlags != 0 && entityUidArg == 0L -> true

                    //Person always has permission to edit their own profile
                    entityUidArg != 0L && activeUserPersonUid == entityUidArg -> true

                    //If adding a new person, then ADD_PERSON permission is required
                    entityUidArg == 0L -> {
                        db.systemPermissionDao().personHasSystemPermission(
                            activeUserPersonUid, PermissionFlags.ADD_PERSON
                        )
                    }

                    //If editing an existing person, which is not the active user, require edit all person permission
                    else -> {
                        db.systemPermissionDao().personHasSystemPermission(
                            accountPersonUid = activeUserPersonUid,
                            permission = PermissionFlags.EDIT_ALL_PERSONS,
                        )
                    }
                }
            },
            onSetFieldsEnabled = {
                _uiState.update { prev -> prev.copy(fieldsEnabled = it) }
            },
            setLoadingState = true,
        ) {
            awaitAll(
                async {
                    loadEntity(
                        serializer = Person.serializer(),
                        //If in registration mode, we should avoid attempting to connect ot the database at all
                        onLoadFromDb = {
                            it.personDao().takeIf { entityUid != 0L }?.findByUidAsync(entityUid)
                        },
                        makeDefault = {
                            Person().also {
                                it.dateOfBirth = savedStateHandle[ARG_DATE_OF_BIRTH]?.toLong() ?: 0L
                            }
                        },
                        uiUpdate = { entityToDisplay ->
                            _uiState.update { it.copy(person = entityToDisplay) }
                        }
                    )
                },
                async {
                    loadEntity(
                        serializer = PersonPicture.serializer(),
                        loadFromStateKeys =listOf(STATE_KEY_PICTURE),
                        onLoadFromDb = if(entityUid != 0L){
                            { it.personPictureDao().findByPersonUidAsync(entityUid) }
                        } else {
                            null
                        },
                        makeDefault = {
                            null
                        },
                        uiUpdate = { personPicture ->
                            _uiState.update { it.copy(personPicture = personPicture) }
                        }
                    ).also {
                        savedStateHandle.setIfNoValueSetYet(INIT_PIC_URI, it?.personPictureUri ?: "")
                    }
                }
            )

            _appUiState.update { prev ->
                prev.copy(
                    actionBarButtonState = ActionBarButtonUiState(
                        visible = true,
                        text = if(registrationModeFlags.hasFlag(REGISTER_MODE_ENABLED)) {
                            systemImpl.getString(MR.strings.register)
                        }else {
                            systemImpl.getString(MR.strings.save)
                        },
                        onClick = this@PersonEditViewModel::onClickSave
                    )
                )
            }

            val personParentJoin = if(registrationModeFlags.hasFlag(REGISTER_MODE_MINOR)) {
                PersonParentJoin()
            }else {
                null
            }

            _uiState.update { prev ->
                prev.copy(
                    approvalPersonParentJoin = personParentJoin,
                    fieldsEnabled = true,
                )
            }
            loadingState = LoadingUiState.NOT_LOADING
        }
    }

    fun onEntityChanged(entity: Person?) {
        _uiState.update { prev ->
            prev.copy(
                person = entity,
                genderError = updateErrorMessageOnChange(prev.person?.gender,
                    entity?.gender, prev.genderError),
                firstNameError = updateErrorMessageOnChange(prev.person?.firstNames,
                    entity?.firstNames, prev.firstNameError),
                lastNameError = updateErrorMessageOnChange(prev.person?.lastName,
                    entity?.lastName, prev.lastNameError),
                phoneNumError = updateErrorMessageOnChange(prev.person?.phoneNum,
                    entity?.phoneNum, prev.phoneNumError),
                emailError = updateErrorMessageOnChange(prev.person?.emailAddr,
                    entity?.emailAddr, prev.emailError),
                usernameError = updateErrorMessageOnChange(prev.person?.username,
                    entity?.username, prev.usernameError),
            )
        }

        scheduleEntityCommitToSavedState(entity, serializer = Person.serializer(),
            commitDelay = 200)
    }

    fun onPasswordChanged(password: String?) {
        _uiState.update { prev ->
            prev.copy(
                password = password,
                passwordError = null,
            )
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

    fun onApprovalPersonParentJoinChanged(personParentJoin: PersonParentJoin?) {
        _uiState.update {prev ->
            prev.copy(approvalPersonParentJoin = personParentJoin)
        }
    }

    private fun PersonEditUiState.hasErrors(): Boolean {
        return usernameError != null ||
            passwordError != null ||
            confirmError != null ||
            dateOfBirthError != null ||
            firstNameError != null ||
            lastNameError != null ||
            genderError != null ||
            emailError != null ||
            parentContactError != null ||
            phoneNumError != null
    }

    private fun validateUsername(username: String): Boolean {
        var isValid = true

        if (username.isNullOrEmpty()){
            isValid = false
        }

        if (isValid){
            if (username.contains(" ")){
                isValid = false
            }
        }

        if (isValid){
            var usernameChars = username.toCharArray()

            for(i in 1..<usernameChars.count()){
                if(usernameChars[i].isUpperCase()){
                    isValid = false
                }
            }
        }

        return isValid
    }

    fun onNationalPhoneNumSetChanged(phoneNumSet: Boolean) {
        _uiState.takeIf { it.value.nationalPhoneNumSet != phoneNumSet }?.update { prev ->
            prev.copy(nationalPhoneNumSet = phoneNumSet)
        }
    }

    fun onClickSave() {
        if(!_uiState.value.fieldsEnabled)
            return

        _uiState.update { prev ->
            prev.copy(fieldsEnabled = false)
        }

        loadingState = LoadingUiState.INDETERMINATE
        _uiState.update { prev -> prev.copy(fieldsEnabled = false) }
        val savePerson = _uiState.value.person?.shallowCopy {
            phoneNum = phoneNum?.trim()?.replace(" ", "")
            emailAddr = emailAddr?.trim()
        } ?: return

        val requiredFieldMessage = systemImpl.getString(MR.strings.field_required_prompt)
        val currentTime = systemTimeInMillis()
        val isRegistrationMode = registrationModeFlags.hasFlag(REGISTER_MODE_ENABLED)
        val validatedEmailAddr = savePerson.emailAddr?.let { validateEmailUseCase(it) }

        _uiState.update { prev ->
            prev.copy(
                usernameError = if(isRegistrationMode && !validateUsername(savePerson.username ?: "")) {
                    systemImpl.getString(MR.strings.invalid)
                }else {
                    null
                },
                passwordError = if(isRegistrationMode && savePerson.username.isNullOrBlank()) {
                    systemImpl.getString(MR.strings.field_required_prompt)
                }else {
                    null
                },
                confirmError = null,
                dateOfBirthError = if(savePerson.dateOfBirth > currentTime) {
                    systemImpl.getString(MR.strings.invalid)
                }else if(isRegistrationMode && !savePerson.dateOfBirth.isDateSet()) {
                    systemImpl.getString(MR.strings.field_required_prompt)
                }else {
                    null
                },
                parentContactError = null,
                firstNameError = if(savePerson.firstNames.isNullOrEmpty()) requiredFieldMessage else null,
                lastNameError = if(savePerson.lastName.isNullOrEmpty()) requiredFieldMessage else null,
                genderError = if(savePerson.gender == GENDER_UNSET) requiredFieldMessage else null,
                phoneNumError = if(_uiState.value.nationalPhoneNumSet &&
                    savePerson.phoneNum?.let { phoneNumValidatorUseCase.isValid(it) } != true
                ) {
                    systemImpl.getString(MR.strings.invalid)
                }else {
                    null
                },
                emailError = if(!savePerson.emailAddr.isNullOrBlank() && validatedEmailAddr == null) {
                    systemImpl.getString(MR.strings.invalid)
                }else {
                    null
                }
            )
        }

        if(_uiState.value.hasErrors()) {
            loadingState = LoadingUiState.NOT_LOADING
            _uiState.update { prev -> prev.copy(fieldsEnabled = true) }
            return
        }

        viewModelScope.launch {
            if(isRegistrationMode) {
                val parentJoin = _uiState.value.approvalPersonParentJoin
                val passwordVal = _uiState.value.password
                val checkedUiState = _uiState.updateAndGet { prev ->
                    prev.copy(
                        usernameError = if(savePerson.username.isNullOrEmpty()) {
                            requiredFieldMessage
                        }else {
                            null
                        },
                        passwordError = if(passwordVal.isNullOrEmpty()) {
                            requiredFieldMessage
                        }else {
                            null
                        },
                        parentContactError = when {
                            !registrationModeFlags.hasFlag(REGISTER_MODE_MINOR) -> null
                            parentJoin?.ppjEmail.isNullOrEmpty() -> requiredFieldMessage
                            parentJoin?.ppjEmail?.let { validateEmailUseCase(it) } == null -> {
                                systemImpl.getString(MR.strings.invalid_email)
                            }
                            else -> null
                        },
                        dateOfBirthError = if(savePerson.dateOfBirth == 0L) {
                            requiredFieldMessage
                        } else {
                            null
                        }
                    )
                }

                if(checkedUiState.hasErrors() || passwordVal == null) {
                    loadingState = LoadingUiState.NOT_LOADING
                    _uiState.update { prev -> prev.copy(fieldsEnabled = true) }
                    return@launch
                }

                try {
                    val registeredPerson = accountManager.register(
                        person = savePerson,
                        password = passwordVal,
                        endpointUrl = serverUrl,
                        accountRegisterOptions = AccountRegisterOptions(
                            makeAccountActive = !registrationModeFlags.hasFlag(REGISTER_MODE_MINOR)
                                    && !dontSetCurrentSession,
                            parentJoin = parentJoin
                        ),
                    )

                    if(registrationModeFlags.hasFlag(REGISTER_MODE_MINOR)) {
                        val goOptions = UstadMobileSystemCommon.UstadGoOptions(
                            RegisterAgeRedirectViewModel.DEST_NAME, true)
                        val args = mutableMapOf<String, String>().also {
                            it[RegisterMinorWaitForParentViewModel.ARG_USERNAME] = savePerson.username ?: ""
                            it[RegisterMinorWaitForParentViewModel.ARG_PARENT_CONTACT] =
                                parentJoin?.ppjEmail ?: ""
                            it[RegisterMinorWaitForParentViewModel.ARG_PASSWORD] = _uiState.value.password ?: ""
                            it.putFromSavedStateIfPresent(savedStateHandle, UstadView.ARG_POPUPTO_ON_FINISH)
                        }

                        navController.navigate(RegisterMinorWaitForParentViewModel.DEST_NAME, args,
                            goOptions)
                    }else {
                        navController.navigateToViewUri(
                            viewUri = nextDestination.appendSelectedAccount(
                                registeredPerson.personUid, Endpoint(serverUrl)
                            ),
                            goOptions = UstadMobileSystemCommon.UstadGoOptions(
                                clearStack = true
                            )
                        )
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
                }finally {
                    loadingState = LoadingUiState.NOT_LOADING
                    _uiState.update { prev -> prev.copy(fieldsEnabled = true) }
                }
            }else {
                //Not register mode

                //If updating an existing person, and the person was not a minor before but is now,
                //and there is no existing consent entity, then we need to create one
                val consentToUpsert = if(entityUidArg != 0L &&
                    Instant.fromEpochMilliseconds(savePerson.dateOfBirth).isDateOfBirthAMinor() &&
                    !Instant.fromEpochMilliseconds(
                        savedStateHandle[KEY_INIT_DATE_OF_BIRTH]?.toLong() ?: 0
                    ).isDateOfBirthAMinor() &&
                    !activeRepoWithFallback.personParentJoinDao().isMinorApproved(savePerson.personUid)
                ) {
                    PersonParentJoin().apply {
                        ppjMinorPersonUid = savePerson.personUid
                        ppjParentPersonUid = accountManager.currentAccount.personUid
                        ppjStatus = PersonParentJoin.STATUS_APPROVED
                        ppjApprovalTiemstamp = systemTimeInMillis()
                    }
                }else {
                    null
                }

                activeRepoWithFallback.withDoorTransactionAsync {
                    if(entityUidArg == 0L) {
                        addNewPersonUseCase(
                            person = savePerson,
                            addedByPersonUid = activeUserPersonUid,
                            createPersonParentApprovalIfMinor = true,
                        )
                    }else {
                        activeRepoWithFallback.personDao().updateAsync(savePerson)
                        consentToUpsert?.also {
                            activeRepoWithFallback.personParentJoinDao().upsertAsync(it)
                        }
                    }
                }

                val personPictureVal = _uiState.value.personPicture

                if(personPictureVal != null) {
                    personPictureVal.personPictureUid = savePerson.personUid
                    personPictureVal.personPictureLct = systemTimeInMillis()
                    val initPictureUri = savedStateHandle[INIT_PIC_URI] ?: ""
                    val personPictureUriVal = personPictureVal.personPictureUri
                    if(initPictureUri != personPictureUriVal) {
                        //Save if changed
                        activeDb.personPictureDao().upsert(personPictureVal)
                        enqueueSavePictureUseCase(
                            entityUid = savePerson.personUid,
                            tableId = PersonPicture.TABLE_ID,
                            pictureUri = personPictureUriVal
                        )
                    }
                }

                //Handle the following scenario: ClazzMemberList (user selects to add a student to enrol),
                // PersonList, PersonEdit, EnrolmentEdit
                val goToOnPersonSelected = savedStateHandle[ARG_GO_TO_ON_PERSON_SELECTED]

                if(goToOnPersonSelected != null) {
                    val args = UMFileUtil.parseURLQueryString(goToOnPersonSelected) +
                        mapOf(UstadView.ARG_PERSON_UID to savePerson.personUid.toString())
                    navController.navigate(goToOnPersonSelected.substringBefore("?"), args)
                }else {
                    finishWithResult(PersonDetailViewModel.DEST_NAME, savePerson.personUid, savePerson)
                }
            }
        }


    }

    companion object {

        const val STATE_KEY_PICTURE = "picState"

        const val DEST_NAME = "PersonEditView"

        /** This is a different view name that is mapped to a different NavController destination
         * This allows it to be recognized for purposes of controlling the visibility of the bottom
         * navigation bar
         */
        const val DEST_NAME_REGISTER = "Register"

        val ALL_DEST_NAMES = listOf(DEST_NAME, DEST_NAME_REGISTER)


        /**
         * If true, the view will show space for the user to enter a username and password to register.
         * The presenter will then register the new user with the server (provided via ARG_SERVER_URL)
         */
        const val ARG_REGISTRATION_MODE = "RegMode"

        /**
         * If the form is in registration mode, then the date of birth must be supplied as an
         * argument.
         */
        const val ARG_DATE_OF_BIRTH = "DateOfBirth"

        /**
         * If this is set then this means that the person registering has come from a link. Since someone in the system has invited another person
         * we use this flag to remove the age restrictions of being under 13 to sign up.
         */
        const val REGISTER_VIA_LINK = "RegViaLink"

        /**
         * Registration mode argument value indicating that this is not being used in registration mode
         */
        const val REGISTER_MODE_NONE = 0

        /**
         * Registration mode argument value indicating that this is being used to register a user
         * who is not a minor (age > 13)
         */
        const val REGISTER_MODE_ENABLED = 1

        /**
         * Registration mode argument value indicating that a minor is being registered
         */
        const val REGISTER_MODE_MINOR = 2


        /**
         * Arguments that must be passed from the login screen through age redirect and terms
         * acceptance to this screen (PersonEdit) in order to register.
         */
        val REGISTRATION_ARGS_TO_PASS = listOf(
            UstadView.ARG_API_URL,
            SiteTermsDetailView.ARG_SHOW_ACCEPT_BUTTON,
            UstadView.ARG_POPUPTO_ON_FINISH,
            ARG_NEXT,
            REGISTER_VIA_LINK,
            ARG_DATE_OF_BIRTH,
            ARG_REGISTRATION_MODE,
            ARG_DONT_SET_CURRENT_SESSION,
        )

        /**
         * Used to store the date of birth on first load so that we can determine if a date of birth
         * update makes the person a minor.
         */
        const val KEY_INIT_DATE_OF_BIRTH = "initDob"

    }

}