package com.ustadmobile.core.viewmodel.signup

import com.ustadmobile.core.MR
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase
import com.ustadmobile.core.domain.passkey.CreatePasskeyParams
import com.ustadmobile.core.domain.passkey.CreatePasskeyUseCase
import com.ustadmobile.core.domain.passkey.PasskeyResult
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.config.ApiUrlConfig
import com.ustadmobile.core.impl.config.GenderConfig
import com.ustadmobile.core.impl.locale.entityconstants.PersonConstants
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.person.child.AddChildProfileViewModel
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel
import com.ustadmobile.door.ext.doorIdentityHashCode
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_UNSET
import com.ustadmobile.lib.db.entities.PersonPicture
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on


data class SignUpUiState(

    val person: Person? = null,

    val password: String? = null,

    val genderOptions: List<MessageIdOption2> = PersonConstants.GENDER_MESSAGE_IDS_AND_UNSET,

    val personPicture: PersonPicture? = null,

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

    val isParent: Boolean? = false,

    val isTeacher: Boolean? = false,

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

    val createPasskeyUseCase: CreatePasskeyUseCase by di.instance()


    val uiState: Flow<SignUpUiState> = _uiState.asStateFlow()

    private val apiUrlConfig: ApiUrlConfig by instance()

    private val entityUid: Long
        get() = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0

    private val serverUrl = savedStateHandle[UstadView.ARG_API_URL]
        ?: apiUrlConfig.presetApiUrl ?: "http://localhost"
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
                person = Person(dateOfBirth = savedStateHandle[PersonEditViewModel.ARG_DATE_OF_BIRTH]?.toLong() ?: 0L),
                serverUrl_ = serverUrl

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
                firstNameError = updateErrorMessageOnChange(
                    prev.person?.firstNames,
                    entity?.firstNames, prev.firstNameError
                ),
                lastNameError = updateErrorMessageOnChange(
                    prev.person?.lastName,
                    entity?.lastName, prev.lastNameError
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


    private fun SignUpUiState.hasErrors(): Boolean {
        return firstNameError != null ||
                lastNameError != null ||
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
        val savePerson = _uiState.value.person ?: return

        val requiredFieldMessage = systemImpl.getString(MR.strings.field_required_prompt)

        _uiState.update { prev ->
            prev.copy(
                firstNameError = if (savePerson.firstNames.isNullOrEmpty()) requiredFieldMessage else null,
                lastNameError = if (savePerson.lastName.isNullOrEmpty()) requiredFieldMessage else null,
                genderError = if (savePerson.gender == GENDER_UNSET) requiredFieldMessage else null,
            )
        }

        if (_uiState.value.hasErrors()) {
            loadingState = LoadingUiState.NOT_LOADING
            return
        }

        viewModelScope.launch {
            val passwordVal = _uiState.value.password
//            val checkedUiState = _uiState.updateAndGet { prev ->
//                prev.copy(
//                    usernameError = if (savePerson.username.isNullOrEmpty()) {
//                        requiredFieldMessage
//                    } else {
//                        null
//                    },
//                    passwordError = if (passwordVal.isNullOrEmpty()) {
//                        requiredFieldMessage
//                    } else {
//                        null
//                    },
//
//                    dateOfBirthError = if (savePerson.dateOfBirth == 0L) {
//                        requiredFieldMessage
//                    } else {
//                        null
//                    }
//                )
//            }
//
//            if (checkedUiState.hasErrors()) {
//                loadingState = LoadingUiState.NOT_LOADING
//                return@launch
//            }

            try {

                val uid = activeDb.doorPrimaryKeyManager.nextIdAsync(Person.TABLE_ID)
                savePerson.personUid = uid
                Napier.e { "person uid $uid" }
                val passkeyCreated = createPasskeyUseCase.invoke(
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
                val result = passkeyCreated?.let {
                    accountManager.registerWithPasskey(
                        serverUrl,
                        it,
                        savePerson,
                        _uiState.value.personPicture
                    )
                }
                Napier.e { "passkeyuid $result" }
                if (result != null) {
//                    val personid = addNewPersonUseCase(
//                        person = savePerson,
//                        addedByPersonUid = activeUserPersonUid,
//                        createPersonParentApprovalIfMinor = true,
//                    )
                    val personPictureVal = _uiState.value.personPicture

                    if (personPictureVal != null) {
                        personPictureVal.personPictureUid = savePerson.personUid
                        personPictureVal.personPictureLct = systemTimeInMillis()
                        val personPictureUriVal = personPictureVal.personPictureUri

                       // activeDb.personPictureDao().upsert(personPictureVal)
                        enqueueSavePictureUseCase(
                            entityUid = savePerson.personUid,
                            tableId = PersonPicture.TABLE_ID,
                            pictureUri = personPictureUriVal
                        )
                        navController.navigate(AddChildProfileViewModel.DEST_NAME, emptyMap())

                    }
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

    fun onPassKeyDataReceived(passkeyResult: PasskeyResult) {


    }

    fun onPassKeyError(passkeyError: String) {
        snackDispatcher.showSnackBar(
            Snack(passkeyError)
        )
    }
    fun onClickOtherOption(){

        navController.navigate(AddChildProfileViewModel.DEST_NAME, emptyMap())

    }

    companion object {

        const val STATE_KEY_PICTURE = "picState"

        const val DEST_NAME = "SignUp"

        const val ARG_DATE_OF_BIRTH = "DateOfBirth"


    }

}