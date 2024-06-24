package com.ustadmobile.core.viewmodel

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import com.ustadmobile.core.domain.language.SetLanguageUseCase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.individual.IndividualLearnerViewModel
import com.ustadmobile.core.viewmodel.redirect.RedirectViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import org.kodein.di.instance

data class OnboardingUiState(
    val currentLanguage: UstadMobileSystemCommon.UiLanguage = UstadMobileSystemCommon.UiLanguage("en", "English"),
    val languageList: List<UstadMobileSystemCommon.UiLanguage> = listOf(currentLanguage),
    val showWaitForRestart: Boolean = false,
)

class OnBoardingViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(OnboardingUiState())

    private val supportLangConfig: SupportedLanguagesConfig by instance()

    private val setLanguageUseCase: SetLanguageUseCase by instance()

    private val settings: Settings by instance()

    val uiState: Flow<OnboardingUiState>
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
            OnboardingUiState(currentLanguage, allLanguages)
        }
    }

    fun onClickExistJoining(){
        settings[PREF_TAG] =  true.toString()
        navController.navigate(RedirectViewModel.DEST_NAME, buildMap {
            putFromSavedStateIfPresent(ARG_NEXT)
            putFromSavedStateIfPresent(ARG_OPEN_LINK)
        })
    }


    fun onClickIndividual() {
        navController.navigate(IndividualLearnerViewModel.DEST_NAME, buildMap {
        })
    }
    fun onClickAddNewOrganization() {
        //Redirect to website (ask mike that is there prebuild util class to open website)
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

        const val DEST_NAME = "Onboarding"
        const val PREF_TAG = "onboaring_screen"

    }
}