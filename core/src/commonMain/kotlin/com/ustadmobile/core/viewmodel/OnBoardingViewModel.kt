package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

data class OnboardingUiState(
    val currentLanguage: UstadMobileSystemCommon.UiLanguage = UstadMobileSystemCommon.UiLanguage("en", "English"),
    val languageList: List<UstadMobileSystemCommon.UiLanguage> = listOf(currentLanguage),
)

class OnBoardingViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(OnboardingUiState())

    private val supportLangConfig: SupportedLanguagesConfig by instance()

    val uiState: Flow<OnboardingUiState>
        get() = _uiState.asStateFlow()

    init {
        val allLanguages = supportLangConfig.supportedUiLanguagesAndSysDefault(systemImpl)
        val currentLocaleCode = systemImpl.getLocale()
        val currentLanguage = allLanguages.first { it.langCode == currentLocaleCode}

        _uiState.update {
            OnboardingUiState(currentLanguage, allLanguages)
        }
    }

    fun onClickNext(){
        val systemImpl: UstadMobileSystemImpl = di.direct.instance()
        systemImpl.setAppPref(PREF_TAG, true.toString())
    }

    fun onLanguageSelected(uiLanguage: UstadMobileSystemCommon.UiLanguage) {
        systemImpl.setLocale(uiLanguage.langCode)
        _uiState.update { previous ->
            previous.copy(currentLanguage =  uiLanguage)
        }
    }

    companion object {

        const val DEST_NAME = "Onboarding"

        const val PREF_TAG = "onboaring_screen"

    }
}