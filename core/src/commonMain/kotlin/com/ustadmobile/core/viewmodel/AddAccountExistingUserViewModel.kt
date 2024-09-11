package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.requireHttpPrefix
import com.ustadmobile.core.util.ext.requirePostfix
import com.ustadmobile.core.util.ext.verifySite
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEARNINGSPACE_URL
import com.ustadmobile.core.view.UstadView.Companion.ARG_SITE
import com.ustadmobile.core.viewmodel.individual.IndividualLearnerViewModel
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import com.ustadmobile.core.viewmodel.person.learningspacelist.LearningSpaceListViewModel
import com.ustadmobile.core.viewmodel.person.registerageredirect.RegisterAgeRedirectViewModel
import com.ustadmobile.core.viewmodel.signup.SignUpViewModel
import com.ustadmobile.core.viewmodel.signup.SignUpViewModel.Companion.ARG_IS_PERSONAL_ACCOUNT
import com.ustadmobile.core.viewmodel.siteenterlink.LearningSpaceEnterLinkViewModel.Companion.ARGS_TO_PASS_THROUGH
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import org.kodein.di.DI
import org.kodein.di.instance

data class AddAccountExistingUserUiState(
    val showWaitForRestart: Boolean = false,
    val showAddPersonalAccount: Boolean = false,

    )

class AddAccountExistingUserViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
) : UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(AddAccountExistingUserUiState())

    private val apiUrlConfig: SystemUrlConfig by instance()


    val uiState: Flow<AddAccountExistingUserUiState>
        get() = _uiState.asStateFlow()

    init {
        _appUiState.value = AppUiState(
            navigationVisible = false,
            hideAppBar = false,
            userAccountIconVisible = false,
            title = systemImpl.getString(MR.strings.existing_user),
        )
        if (apiUrlConfig.newPersonalAccountsLearningSpaceUrl != null) {
            _uiState.update { prev ->
                prev.copy(
                    showAddPersonalAccount = true
                )
            }
        }
    }

    fun onClickIndividual() {
        navController.navigate(IndividualLearnerViewModel.DEST_NAME, emptyMap())
    }

    fun onClickLearningSpace() {
        navController.navigate(
            LearningSpaceListViewModel.DEST_NAME,
            args = buildMap {
                putFromSavedStateIfPresent(SignUpViewModel.REGISTRATION_ARGS_TO_PASS)
            }
        )
    }

    fun onClickPersonalAccount() {
        val args = buildMap {
            put(
                ARG_LEARNINGSPACE_URL,
                apiUrlConfig.newPersonalAccountsLearningSpaceUrl?.requireHttpPrefix()
                    ?.requirePostfix("/") ?: ""
            )
        }

        navController.navigate(LoginViewModel.DEST_NAME, args)

    }

    companion object {

        const val DEST_NAME = "AddAccountExistingUser"
        const val PREF_TAG = "AddAccountExistingUser_screen"

    }
}