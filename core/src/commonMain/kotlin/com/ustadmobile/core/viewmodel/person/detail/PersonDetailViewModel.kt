package com.ustadmobile.core.viewmodel.person.detail

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.MR
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.domain.phonenumber.IPhoneNumberUtil
import com.ustadmobile.core.domain.phonenumber.OnClickPhoneNumUseCase
import com.ustadmobile.core.domain.phonenumber.formatInternationalOrNull
import com.ustadmobile.core.domain.sendemail.OnClickEmailUseCase
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState.Companion.INDETERMINATE
import com.ustadmobile.core.impl.appstate.LoadingUiState.Companion.NOT_LOADING
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.domain.sms.OnClickSendSmsUseCase
import com.ustadmobile.core.util.ext.isDateSet
import com.ustadmobile.core.util.ext.personFullName
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.CURRENT_DEST
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.core.viewmodel.parentalconsentmanagement.ParentalConsentManagementViewModel
import com.ustadmobile.core.viewmodel.clazz.detail.ClazzDetailViewModel
import com.ustadmobile.core.viewmodel.message.messagelist.MessageListViewModel
import com.ustadmobile.core.viewmodel.person.accountedit.PersonAccountEditViewModel
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel
import com.ustadmobile.core.viewmodel.systempermission.detail.SystemPermissionDetailViewModel
import com.ustadmobile.lib.db.composites.ClazzEnrolmentAndPersonDetailDetails
import org.kodein.di.instanceOrNull

data class PersonDetailUiState(

    val person: PersonAndDisplayDetail? = null,

    val displayPhoneNum: String? = null,

    val canSendSms: Boolean = false,

    val clazzes: List<ClazzEnrolmentAndPersonDetailDetails> = emptyList(),

    internal val hasChangePasswordPermission: Boolean = false,

    val showPermissionButton: Boolean = false,

    val isActiveUser: Boolean = false,

) {

    val dateOfBirthVisible: Boolean
        get() = person?.person?.dateOfBirth.isDateSet()

    val personGenderVisible: Boolean
        get() = person?.person?.gender  != null
                && person.person?.gender != 0

    val changePasswordVisible: Boolean
        get() = person?.person?.username != null && hasChangePasswordPermission

    val showCreateAccountVisible: Boolean
        get() = person != null && person.person?.username == null && hasChangePasswordPermission

    val personAddressVisible: Boolean
        get() = !person?.person?.personAddress.isNullOrBlank()

    val phoneNumVisible: Boolean
        get() = !person?.person?.phoneNum.isNullOrBlank()

    val emailVisible: Boolean
        get() = !person?.person?.emailAddr.isNullOrBlank()

    val personOrgIdVisible: Boolean
        get() = !person?.person?.personOrgId.isNullOrBlank()

    val personUsernameVisible: Boolean
        get() = !person?.person?.username.isNullOrBlank()

    val manageParentalConsentVisible: Boolean
        get() = person?.parentJoin != null

    val sendSmsVisible: Boolean
        get() = canSendSms && !person?.person?.phoneNum.isNullOrBlank()

    val chatVisible: Boolean
        get() = !person?.person?.username.isNullOrBlank() && !isActiveUser

}

class PersonDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): DetailViewModel<Person>(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(PersonDetailUiState())

    val uiState: Flow<PersonDetailUiState> = _uiState.asStateFlow()

    private val personUid = savedStateHandle[ARG_ENTITY_UID]?.toLong() ?: 0

    private val phoneNumberUtil: IPhoneNumberUtil? by instanceOrNull()

    private val onClickPhoneNumUseCase: OnClickPhoneNumUseCase by instance()

    //Will be null on platforms that don't support sms
    private val onClickSendSmsUseCase: OnClickSendSmsUseCase? by instanceOrNull()

    private val onClickEmailUseCase: OnClickEmailUseCase by instance()

    init {
        val accountManager: UstadAccountManager by instance()

        _appUiState.update { prev ->
            prev.copy(
                loadingState = INDETERMINATE,
                fabState = FabUiState(
                    visible = false,
                    text = systemImpl.getString(MR.strings.edit),
                    icon = FabUiState.FabIcon.EDIT,
                    onClick = this::onClickEdit,
                )
            )
        }

        _uiState.update { prev ->
            prev.copy(
                isActiveUser = entityUidArg == activeUserPersonUid,
                canSendSms = onClickSendSmsUseCase != null
            )
        }

        viewModelScope.launch {
            val entityFlow = activeRepoWithFallback.personDao().findByUidWithDisplayDetailsFlow(
                personUid = entityUidArg,
                accountPersonUid = activeUserPersonUid,
            )

            val viewAndEditPermissionFlow = activeRepoWithFallback.systemPermissionDao()
                .personHasEditAndViewPermissionForPersonAsFlow(
                    accountPersonUid = activeUserPersonUid,
                    otherPersonUid = entityUidArg
                ).shareIn(viewModelScope, SharingStarted.WhileSubscribed())

            //Any required SystemPermission entities will be pulled down by viewAndEditPermissionFlow
            //So hasSystemPermissionFlow can use db instead of repo
            val hasManagePermissionsPermissionFlow = activeDb.systemPermissionDao()
                .personHasSystemPermissionAsFlow(
                    accountPersonUid = activeUserPersonUid,
                    permission = PermissionFlags.MANAGE_USER_PERMISSIONS,
                )

            _uiState.whenSubscribed {
                launch {
                    entityFlow.combine(viewAndEditPermissionFlow) { entity, permissions ->
                        entity.takeIf { permissions.hasViewPermission }
                    }.collect { person ->
                        _uiState.update { prev ->
                            prev.copy(
                                person = person,
                                displayPhoneNum = person?.person?.phoneNum?.let {
                                    phoneNumberUtil?.formatInternationalOrNull(it)
                                }
                            )
                        }

                        _appUiState.update { prev ->
                            prev.copy(
                                title = person?.person?.personFullName() ?: "",
                                loadingState = if(person != null) { NOT_LOADING } else { INDETERMINATE }
                            )
                        }
                    }
                }

                launch {
                    hasManagePermissionsPermissionFlow.distinctUntilChanged().collect {
                        _uiState.update { prev -> prev.copy(showPermissionButton = it) }
                    }
                }

                launch {
                    viewAndEditPermissionFlow.collect {
                        _uiState.update { prev ->
                            prev.copy(
                                hasChangePasswordPermission = it.hasEditPermission
                            )
                        }

                        _appUiState.update { prev ->
                            prev.copy(
                                fabState = if(it.hasEditPermission) {
                                    FabUiState(
                                        visible = true,
                                        text = systemImpl.getString(MR.strings.edit),
                                        icon = FabUiState.FabIcon.EDIT,
                                        onClick = this@PersonDetailViewModel::onClickEdit
                                    )
                                }else {
                                    FabUiState()
                                }
                            )
                        }
                    }
                }

                launch {
                    activeDb.clazzEnrolmentDao().findAllClazzesByPersonWithClazz(
                        accountPersonUid = activeUserPersonUid,
                        otherPersonUid = entityUidArg
                    ).collect {
                        _uiState.update { prev ->
                            prev.copy(
                                clazzes = it
                            )
                        }
                    }
                }
            }
        }
    }

    fun onClickEdit() {
        navController.navigate(PersonEditViewModel.DEST_NAME,
            mapOf(ARG_ENTITY_UID to personUid.toString()))
    }

    fun onClickClazz(clazz: ClazzEnrolmentAndPersonDetailDetails) {
        val clazzUid = clazz.clazz?.clazzUid ?: return
        navController.navigate(
            ClazzDetailViewModel.DEST_NAME,
            mapOf(ARG_ENTITY_UID to clazzUid.toString())
        )
    }

    private fun navigateToEditAccount() {
        navController.navigate(PersonAccountEditViewModel.DEST_NAME,
            mapOf(ARG_ENTITY_UID to personUid.toString()))
    }

    fun onClickCreateAccount() = navigateToEditAccount()

    fun onClickChangePassword() = navigateToEditAccount()

    fun onClickChat() {
        navController.navigate(
            MessageListViewModel.DEST_NAME,
            mapOf(UstadView.ARG_PERSON_UID to personUid.toString())
        )
    }

    fun onClickDial() {
        _uiState.value.person?.person?.phoneNum?.also {
            onClickPhoneNumUseCase(it)
        }
    }

    fun onClickSms() {
        _uiState.value.person?.person?.phoneNum?.also {
            onClickSendSmsUseCase?.onClickSendSms(it)
        }
    }

    fun onClickEmail() {
        _uiState.value.person?.person?.emailAddr?.also {
            onClickEmailUseCase(it)
        }
    }

    fun onClickPermissions() {
        navController.navigate(
            SystemPermissionDetailViewModel.DEST_NAME,
            mapOf(
                ARG_PERSON_UID to entityUidArg.toString()
            )
        )
    }

    fun onClickManageParentalConsent() {
        val ppjUid = _uiState.value.person?.parentJoin?.ppjUid ?: 0L
        if(ppjUid != 0L) {
            navController.navigate(
                ParentalConsentManagementViewModel.DEST_NAME,
                mapOf(ARG_ENTITY_UID to ppjUid.toString(),
                    ARG_NEXT to CURRENT_DEST))
        }
    }

    companion object {

        const val DEST_NAME = "PersonDetailView"

    }
}

