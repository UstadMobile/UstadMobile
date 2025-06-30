package com.ustadmobile.core.viewmodel.signup

import com.ustadmobile.core.MR
import com.ustadmobile.core.account.AccountRegisterOptions
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.account.SendConsentRequestToParentUseCase
import com.ustadmobile.core.db.UmAppDataLayer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase
import com.ustadmobile.core.domain.invite.EnrollToCourseFromInviteCodeUseCase
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.core.domain.credentials.password.SavePasswordUseCase
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
import com.ustadmobile.core.util.ext.putFromSavedStateIfPresent
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.CURRENT_DEST
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.account.addaccountselectneworexisting.AddAccountSelectNewOrExistingViewModel
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.core.viewmodel.parentalconsentmanagement.ParentalConsentManagementViewModel
import com.ustadmobile.core.viewmodel.person.child.ChildProfileListViewModel
import com.ustadmobile.core.viewmodel.person.child.ChildProfileListViewModel.Companion.ARG_CHILD_NAME
import com.ustadmobile.core.viewmodel.person.child.ChildProfileListViewModel.Companion.ARG_PPJ_UID
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel
import com.ustadmobile.core.viewmodel.person.registerminorwaitforparent.RegisterMinorWaitForParentViewModel
import com.ustadmobile.core.viewmodel.person.registerminorwaitforparent.RegisterMinorWaitForParentViewModel.Companion.ARG_REFERER_SCREEN
import com.ustadmobile.core.viewmodel.person.toFirstAndLastNameExt
import com.ustadmobile.core.viewmodel.signup.OtherSignUpOptionSelectionViewModel.Companion.IS_PARENT
import com.ustadmobile.core.viewmodel.signup.SignUpViewModel.Companion.ARG_DATE_OF_BIRTH
import com.ustadmobile.core.viewmodel.signup.SignUpViewModel.Companion.ARG_IS_MINOR
import com.ustadmobile.core.viewmodel.signup.SignUpViewModel.Companion.ARG_IS_PERSONAL_ACCOUNT
import com.ustadmobile.core.viewmodel.signup.SignUpViewModel.Companion.REGISTRATION_ARGS_TO_PASS
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonParentJoin
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


data class SignupEnterUsernamePasswordUiState(

    val person: Person? = null,

    val password: String? = null,

    val genderOptions: List<MessageIdOption2> = PersonConstants.GENDER_MESSAGE_IDS_AND_UNSET,

    val personPicture: PersonPicture? = null,

    val registrationMode: Int = 0,

    val firstName: String? = null,

    val passwordError: String? = null,

    val dateOfBirthError: String? = null,

    val parentContactError: String? = null,

    val genderError: String? = null,

    val fullNameError: String? = null,

    val passkeySupported: Boolean = true,

    val isPersonalAccount: Boolean = false,
)

class SignupEnterUsernamePasswordViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destName: String = DEST_NAME
) : UstadEditViewModel(di, savedStateHandle, destName) {

    private val _uiState: MutableStateFlow<SignupEnterUsernamePasswordUiState> = MutableStateFlow(
        SignupEnterUsernamePasswordUiState()
    )

    private var nextDestination: String =
        savedStateHandle[UstadView.ARG_NEXT] ?: ClazzListViewModel.DEST_NAME_HOME

    val uiState: Flow<SignupEnterUsernamePasswordUiState> = _uiState.asStateFlow()

    private val apiUrlConfig: SystemUrlConfig by instance()

    private val isParent = savedStateHandle[IS_PARENT].toBoolean()

    private val isPersonalAccount = savedStateHandle[ARG_IS_PERSONAL_ACCOUNT].toBoolean()

    private val isMinor = savedStateHandle[ARG_IS_MINOR].toBoolean()

    private val serverUrl = savedStateHandle[UstadView.ARG_LEARNINGSPACE_URL]
        ?: apiUrlConfig.newPersonalAccountsLearningSpaceUrl ?: "http://localhost"

    val addNewPersonUseCase: AddNewPersonUseCase = di.on(LearningSpace(serverUrl)).direct.instance()

    private val genderConfig: GenderConfig by instance()

    private val enrollToCourseFromInviteCodeUseCase:EnrollToCourseFromInviteCodeUseCase =
    di.on(LearningSpace(serverUrl)).direct.instance()

    //Run EnqueueSavePictureUseCase after the database transaction has finished.
    private val enqueueSavePictureUseCase: EnqueueSavePictureUseCase by
        on(LearningSpace(serverUrl)).instance()

    val sendConsentRequestToParentUseCase : SendConsentRequestToParentUseCase =
        di.on(LearningSpace(serverUrl)).direct.instance()

    val repo: UmAppDatabase = di.on(LearningSpace(serverUrl)).direct.instance<UmAppDataLayer>()
        .requireRepository()

    init {
        loadingState = LoadingUiState.INDETERMINATE
        val title = systemImpl.getString(MR.strings.create_account)
        viewModelScope.launch {
            val person = savedStateHandle.getJson(
                OtherSignUpOptionSelectionViewModel.ARG_PERSON, Person.serializer(),
            ) ?: Person()

            val personPicture = savedStateHandle.getJson(
                OtherSignUpOptionSelectionViewModel.ARG_PERSON_PROFILE_PIC,
                PersonPicture.serializer(),
            )
            _uiState.update { prev ->
                prev.copy(
                    person = person,
                    personPicture = personPicture,
                    firstName = if (person.firstNames == "") {
                        null
                    } else {
                        person.fullName()
                    }


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
                    dateOfBirth = savedStateHandle[PersonEditViewModel.ARG_DATE_OF_BIRTH]?.toLong() ?: 0L,
                    isPersonalAccount = _uiState.value.isPersonalAccount
                ),
            )
        }

    }

    fun onEntityChanged(entity: Person?) {
        _uiState.update { prev ->
            prev.copy(
                person = entity,
                )
        }

        scheduleEntityCommitToSavedState(
            entity, serializer = Person.serializer(),
            commitDelay = 200
        )
    }

    fun onPasswordChanged(password: String?) {
        _uiState.update { prev ->
            prev.copy(
                password = password,
                passwordError = null,
            )
        }
    }


    private fun SignupEnterUsernamePasswordUiState.hasErrors(): Boolean {
        return passwordError != null
    }


    fun onClickedSignupEnterUsernamePassword() {


        loadingState = LoadingUiState.INDETERMINATE

        val fullName = _uiState.value.firstName?.trim()
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

        _uiState.update { prev ->
            prev.copy(

                passwordError = if (prev.password.isNullOrEmpty()) {
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

                val passwordVal = _uiState.value.password ?: return@launch
                val parentJoin = PersonParentJoin(ppjEmail = savedStateHandle[SignUpViewModel.ARG_PARENT_CONTACT])
               val person= accountManager.register(
                    person = savePerson,
                    password = passwordVal,
                    learningSpaceUrl = serverUrl,
                    accountRegisterOptions = AccountRegisterOptions(
                        makeAccountActive = !isMinor,
                        parentJoin = parentJoin
                    ),
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

                val savePasswordUseCase: SavePasswordUseCase? = di.on(LearningSpace(serverUrl))
                    .direct.instanceOrNull()

                savePasswordUseCase?.invoke(
                    username = savePerson.username.toString(),
                    password = passwordVal,
                )

                try {
                    val viewUri = savedStateHandle[UstadView.ARG_NEXT]
                    if (viewUri != null&&viewUri.contains("ClazzInviteRedeem")) {
                        nextDestination = ClazzListViewModel.DEST_NAME_HOME
                        enrollToCourseFromInviteCodeUseCase.invoke(
                            viewUri =viewUri,
                            personUid = savePerson.personUid
                        )
                    }
                } catch (e: Exception) {
                    Napier.d { "enrollToCourseFromInviteCodeUseCase :"+e.message}
                }

                navigateToAppropriateScreen(person)

            } catch (e: Exception) {
                if (e is IllegalStateException) {
                    _uiState.update { prev ->
                        prev.copy(passwordError = systemImpl.getString(MR.strings.person_exists))
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

        if (isParent) {
            if (!isPersonalAccount&&savedStateHandle[ARG_CHILD_NAME]!=null){
                navigateToConsentManagementScreen()
            }else{
                navController.navigate(ChildProfileListViewModel.DEST_NAME,
                    args = buildMap {
                        put(ARG_NEXT, nextDestination)
                        putAllFromSavedStateIfPresent(REGISTRATION_ARGS_TO_PASS)
                        putFromSavedStateIfPresent(ARG_NEXT)
                    }
                )
            }



        } else if (isMinor){
            viewModelScope.launch {
                sendConsentAndNavigateToMinorWaitScreen(true)

            }
        } else {

            val goOptions = UstadMobileSystemCommon.UstadGoOptions(clearStack = true)
            Napier.d { "AddSignupEnterUsernamePasswordPresenter: go to next destination: $nextDestination" }
            navController.navigateToViewUri(
                nextDestination.appendSelectedAccount(
                    savePerson.personUid,
                    LearningSpace(accountManager.activeLearningSpace.url)
                ),
                goOptions
            )

        }
    }

    private fun navigateToConsentManagementScreen() {
        viewModelScope.launch {
            val ppjUid = savedStateHandle[ARG_PPJ_UID]?.toLong()?:0L

            val args = mutableMapOf<String, String>().also {
                it[ARG_ENTITY_UID] = ppjUid.toString()
                it[ARG_NEXT] =CURRENT_DEST
            }
            navController.navigate(
                ParentalConsentManagementViewModel.DEST_NAME,
                args)
        }

    }

    private suspend fun sendConsentAndNavigateToMinorWaitScreen(showUsernamePassword: Boolean) {
        try {
            val savePerson = _uiState.value.person ?: throw IllegalStateException("child details are empty")
            val parentContact = savedStateHandle[SignUpViewModel.ARG_PARENT_CONTACT]

            val personParentJoin = repo.personParentJoinDao()
                .findByMinorPersonUidForConsent(savePerson.personUid)

            sendConsentRequestToParentUseCase(
                SendConsentRequestToParentUseCase.SendConsentRequestToParentRequest(
                    childFullName = savePerson.fullName(),
                    childDateOfBirth = savePerson.dateOfBirth,
                    childGender = savePerson.gender,
                    parentContact = parentContact?:"",
                    ppjUid = personParentJoin.ppjUid
                )
            )
            val args = mutableMapOf<String, String>().also {
                it[RegisterMinorWaitForParentViewModel.ARG_USERNAME] = _uiState.value.person?.username ?: ""
                it[RegisterMinorWaitForParentViewModel.ARG_SHOW_USERNAME_PASSWORD] =
                    showUsernamePassword.toString()
                it[RegisterMinorWaitForParentViewModel.ARG_PARENT_CONTACT] =
                    parentContact?:""
                it[RegisterMinorWaitForParentViewModel.ARG_PASSWORD] =  ""
                it.putFromSavedStateIfPresent(savedStateHandle, UstadView.ARG_POPUPTO_ON_FINISH)
                it[ARG_REFERER_SCREEN] = AddAccountSelectNewOrExistingViewModel.DEST_NAME
            }
            navController.navigate(
                viewName = RegisterMinorWaitForParentViewModel.DEST_NAME,
                args = args,
                goOptions = UstadMobileSystemCommon.UstadGoOptions(clearStack = true)
            )
        } catch (e: Exception) {
            Napier.e("Error : ${e.message}", e)
        }
    }


    companion object {


        const val DEST_NAME = "SignupEnterUsernamePassword"


    }

}