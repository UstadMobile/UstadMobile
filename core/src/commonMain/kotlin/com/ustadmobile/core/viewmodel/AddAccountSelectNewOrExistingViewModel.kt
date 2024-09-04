package com.ustadmobile.core.viewmodel

import com.russhwolf.settings.Settings
import com.ustadmobile.core.domain.language.SetLanguageUseCase
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.SiteTermsDetailView.Companion.ARG_SHOW_ACCEPT_BUTTON
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel
import com.ustadmobile.core.viewmodel.signup.SignUpViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import org.kodein.di.instance

data class AddAccountSelectNewOrExistingUiState(
    val currentLanguage: UstadMobileSystemCommon.UiLanguage = UstadMobileSystemCommon.UiLanguage("en", "English"),
    val languageList: List<UstadMobileSystemCommon.UiLanguage> = listOf(currentLanguage),
    val showWaitForRestart: Boolean = false,
)

/**
 * Allow the user to select "new user" or "existing user" and then take them to the appropriate next
 * screen.
 *
 * Where the learning space URL is known (by ARG_LEARNINGSPACE_URL argument or
 * SystemUrlConfig.presetLearningSpaceUrl is not null):
 *    a) If new account creation is supported by the given learning space, then show the new user
 *       / existing user buttons and allow user to select. When they click an option, take them
 *       directly to the login or signup screen for the given learning space URL.
 *    b) If new account creation is not supported by the given learning space, go directly to the
 *       login screen for the given learning space url. The navigation should pop this screen off (
 *       e.g. going back will not return here)
 *
 * Where the learning space URL is not known (argument not provided and
 * SystemUrlConfig.presetLearningSpaceUrl is null):
 *    a) Show the new user / existing user buttons
 *    b) If SystemUrlConfig.newPersonalAccountsLearningSpaceUrl != null, then go to the Select
 *       personal account or learning space screen.
 *    c) If SystemUrlConfig.newPersonalAccountsLearningSpaceUrl is null, then the system does not
 *       support personal accounts. Go directly to LearningSpaceList screen for the user to select
 *       a learning space.
 */
class AddAccountSelectNewOrExistingViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(AddAccountSelectNewOrExistingUiState())

    private val supportLangConfig: SupportedLanguagesConfig by instance()

    private val setLanguageUseCase: SetLanguageUseCase by instance()

    private val openExternalLinkUseCase: OpenExternalLinkUseCase by instance()


    val uiState: Flow<AddAccountSelectNewOrExistingUiState>
        get() = _uiState.asStateFlow()

    init {
        _appUiState.value = AppUiState(
            navigationVisible = false,
            hideAppBar = true,
        )

        val allLanguages = supportLangConfig
            .supportedUiLanguagesAndSysDefault(systemImpl)
        val currentLanguage = supportLangConfig
            .getCurrentLanguage(systemImpl)

        _uiState.update {
            AddAccountSelectNewOrExistingUiState(currentLanguage, allLanguages)
        }
    }
    fun onClickNewUser(){
        val arg = buildMap {
            putFromSavedStateIfPresent(SignUpViewModel.REGISTRATION_ARGS_TO_PASS)
            put(SignUpViewModel.ARG_NEW_OR_EXISTING_USER,"new")
        }
        navController.navigate(AddAccountSelectNewUserTypeViewModel.DEST_NAME,arg)

    }
    fun onClickExistingUser(){
        val arg = buildMap {
            putFromSavedStateIfPresent(SignUpViewModel.REGISTRATION_ARGS_TO_PASS)
            put(SignUpViewModel.ARG_NEW_OR_EXISTING_USER,"existing")
        }
        navController.navigate(AddAccountExistingUserViewModel.DEST_NAME, arg)

    }



    fun onLanguageSelected(uiLanguage: UstadMobileSystemCommon.UiLanguage) {
        if(uiLanguage != _uiState.value.currentLanguage) {
            val result = setLanguageUseCase(
                uiLanguage, DEST_NAME, navController
            )

            _uiState.update { previous ->
                previous.copy(
                    currentLanguage =  uiLanguage,
                    showWaitForRestart = result.waitForRestart
                )
            }
        }
    }

    companion object {

        const val DEST_NAME = "addAccountSelectNewOrExisting"
        const val PREF_TAG = "addAccountSelectNewOrExisting_screen"

    }
}