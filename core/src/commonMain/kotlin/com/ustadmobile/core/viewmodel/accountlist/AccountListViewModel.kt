package com.ustadmobile.core.viewmodel.accountlist

import com.ustadmobile.core.MR
import com.ustadmobile.core.account.UserSessionWithPersonAndLearningSpace
import com.ustadmobile.core.domain.getversion.GetVersionUseCase
import com.ustadmobile.core.domain.launchopenlicenses.LaunchOpenLicensesUseCase
import com.ustadmobile.core.domain.share.ShareAppUseCase
import com.ustadmobile.core.domain.showpoweredby.GetShowPoweredByUseCase
import com.ustadmobile.core.domain.usersession.StartUserSessionUseCase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.isGuestUser
import com.ustadmobile.core.util.ext.isTemporary
import com.ustadmobile.core.util.ext.navigateToLink
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.about.OpenLicensesViewModel
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import com.ustadmobile.core.viewmodel.person.detail.PersonDetailViewModel
import com.ustadmobile.core.viewmodel.siteenterlink.SiteEnterLinkViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.instanceOrNull

/**
 *
 * @param headerAccount When the AccountList is shown after the user clicks on the icon, then the
 *        active account is shown in the header with buttons to logout and view profile.
 *        When the AccountList is used to get the user to select an account to continue (e.g.
 *        when opened via a link, user logged out of one account but has other active accounts, etc),
 *        then all accounts are just shown in a list and the headerAccount is null.
 *
 * @param showAccountEndpoint where multiple endpoints are supported (see ApiUrlConfig), then we will
 *        show the url that each account is associated with. Otherwise not shown.
 */
data class AccountListUiState(
    val headerAccount: UserSessionWithPersonAndLearningSpace? = null,
    val accountsList: List<UserSessionWithPersonAndLearningSpace> = emptyList(),
    val showAccountEndpoint: Boolean = false,
    val version: String = "",
    val showPoweredBy: Boolean = false,
    val shareAppOptionVisible: Boolean = false,
    val shareAppBottomSheetVisible: Boolean = false
) {

    val activeAccountButtonsEnabled: Boolean
        get() = headerAccount != null && headerAccount.person.personUid != 0L

    val myProfileButtonVisible: Boolean
        get() = headerAccount != null && !headerAccount.person.isGuestUser()

}

class AccountListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    private val startUserSessionUseCase: StartUserSessionUseCase = StartUserSessionUseCase(
        accountManager = di.direct.instance(),
    )
): UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val endpointFilter = savedStateHandle[ARG_FILTER_BY_LEARNINGSPACE]

    private val activeAccountMode =
        savedStateHandle[ARG_ACTIVE_ACCOUNT_MODE] ?: ACTIVE_ACCOUNT_MODE_HEADER

    private val maxDateOfBirth = savedStateHandle[UstadView.ARG_MAX_DATE_OF_BIRTH]?.toLong() ?: 0

    private val apiUrlConfig: SystemUrlConfig by instance()

    private val shareAppUseCase: ShareAppUseCase? by instanceOrNull()

    private val _uiState = MutableStateFlow(
        AccountListUiState(
            showAccountEndpoint = apiUrlConfig.canSelectServer,
            shareAppOptionVisible = shareAppUseCase != null
        )
    )

    val uiState: Flow<AccountListUiState> = _uiState.asStateFlow()

    private val getVersionUseCase: GetVersionUseCase? by instanceOrNull()

    private val launchOpenLicensesUseCase: LaunchOpenLicensesUseCase? by instanceOrNull()

    private val getShowPoweredByUseCase: GetShowPoweredByUseCase? by instanceOrNull()

    private val dontSetCurrentSession: Boolean = savedStateHandle[ARG_DONT_SET_CURRENT_SESSION]
        ?.toBoolean() ?: false

    init {
        _appUiState.value = AppUiState(
            userAccountIconVisible = false,
            navigationVisible = false,
            title = if(savedStateHandle[UstadListViewModel.ARG_LISTMODE] == ListViewMode.PICKER.mode) {
                systemImpl.getString(MR.strings.select_account)
            }else {
                systemImpl.getString(MR.strings.accounts)
            }
        )
        _uiState.update { prev ->
            prev.copy(
                version = getVersionUseCase?.invoke()?.versionString ?: "",
                showPoweredBy = getShowPoweredByUseCase?.invoke() ?: false,
            )
        }

        viewModelScope.takeIf { activeAccountMode == ACTIVE_ACCOUNT_MODE_HEADER }?.launch {
            accountManager.currentUserSessionFlow.collect {
                _uiState.update { prev ->
                    prev.copy(headerAccount = it)
                }
            }
        }

        viewModelScope.launch {
            _uiState.whenSubscribed {
                accountManager.activeUserSessionsFlow.collect { accountList ->
                    val currentUserSessionUid = accountManager.currentUserSession.userSession.usUid
                    val accountsToDisplay = accountList.filter {
                        //Don't show current account when it is shown in the header
                        val isFilteredOutActiveAccount =
                            (activeAccountMode == ACTIVE_ACCOUNT_MODE_HEADER &&
                                    it.userSession.usUid == currentUserSessionUid)
                        val isFilteredOutByEndpoint = (endpointFilter != null && it.learningSpace.url != endpointFilter)
                        val isFilteredOutByDateOfBirth = (maxDateOfBirth > 0 && it.person.dateOfBirth > maxDateOfBirth)

                        !(isFilteredOutActiveAccount ||
                                isFilteredOutByEndpoint ||
                                isFilteredOutByDateOfBirth ||
                                it.userSession.isTemporary())
                    }
                    _uiState.update { prev ->
                        prev.copy(
                            accountsList = accountsToDisplay
                        )
                    }
                }
            }
        }
    }

    fun onClickAppShare(shareLink: Boolean) {
        viewModelScope.launch {
            try {
                shareAppUseCase?.invoke(shareLink)
            } catch (e: Throwable) {
                snackDispatcher.showSnackBar(Snack(e.message.toString()))
            }
        }
    }


    fun onClickLogout() {
        val currentSession = _uiState.value.headerAccount ?: return
        viewModelScope.launch {
            accountManager.endSession(currentSession)

            navController.navigateToLink(
                link = ClazzListViewModel.DEST_NAME_HOME,
                accountManager = accountManager,
                openExternalLinkUseCase = { _, _ ->  },
                userCanSelectServer = apiUrlConfig.canSelectServer,
                goOptions = UstadMobileSystemCommon.UstadGoOptions(
                    clearStack = true,
                )
            )
        }
    }

    fun onClickProfile() {
        val personUid = _uiState.value.headerAccount?.person?.personUid ?: return
        navController.navigate(PersonDetailViewModel.DEST_NAME, mapOf(
            ARG_ENTITY_UID to personUid.toString()
        ))
    }

    fun onClickAddAccount(){
        val args = buildMap {
            if(endpointFilter != null)
                put(ARG_SERVER_URL, endpointFilter)

            putFromSavedStateIfPresent(listOf(ARG_NEXT, ARG_DONT_SET_CURRENT_SESSION))

            put(ARG_MAX_DATE_OF_BIRTH, savedStateHandle[ARG_MAX_DATE_OF_BIRTH] ?: "0")
        }
        if(endpointFilter != null || !apiUrlConfig.canSelectServer) {
            navController.navigate(
                viewName = LoginViewModel.DEST_NAME,
                args = args
            )
        }else {
            //Go to site enter link
            navController.navigate(
                viewName = SiteEnterLinkViewModel.DEST_NAME,
                args = args,
            )
        }
    }

    /**
     * Switch accounts
     */
    fun onClickAccount(sessionWithPersonAndLearningSpace: UserSessionWithPersonAndLearningSpace) {
        startUserSessionUseCase(
            session = sessionWithPersonAndLearningSpace,
            navController = navController,
            nextDest = savedStateHandle[ARG_NEXT] ?: ClazzListViewModel.DEST_NAME_HOME,
            dontSetCurrentSession = dontSetCurrentSession,
        )
    }

    fun onClickDeleteAccount(session: UserSessionWithPersonAndLearningSpace) {
        viewModelScope.launch {
            accountManager.endSession(session)
        }

    }

    fun onClickOpenLicenses() {
        val launchUseCaseVal = launchOpenLicensesUseCase
        if(launchUseCaseVal != null) {
            viewModelScope.launch {
                launchUseCaseVal()
            }
        }else {
            navController.navigate(OpenLicensesViewModel.DEST_NAME, emptyMap())
        }
    }

    fun onToggleShareAppOptions() {
        _uiState.update {
            it.copy(shareAppBottomSheetVisible = !it.shareAppBottomSheetVisible)
        }
    }

    companion object {

        const val DEST_NAME = "AccountList"

        /**
         * Where FILTER_BY_ENDPOINT is specified only accounts for the given endpoint will be
         * displayed. If the user clicks 'add account', the user will be taken directly to the
         * login screen for that server (e.g. they will never be taken to the server selection screen)
         */
        const val ARG_FILTER_BY_LEARNINGSPACE = "filterByLearningSpace"

        /**
         * The Active Account mode can be "header" or "inlist".
         *
         * Header shows the active account at the top with a profile and logout button
         * (e.g. useful for the normal account list page)
         *
         * Inlist shows the active account in the list of accounts itself. This is used when we want
         * the user to explicitly select an account (even if they already have a currently active
         * account).
         */
        const val ARG_ACTIVE_ACCOUNT_MODE = "activeAccountMode"

        const val ACTIVE_ACCOUNT_MODE_HEADER = "header"

        const val ACTIVE_ACCOUNT_MODE_INLIST = "inlist"

    }
}
