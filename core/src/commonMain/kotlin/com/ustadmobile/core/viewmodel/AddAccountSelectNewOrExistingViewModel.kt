package com.ustadmobile.core.viewmodel

import com.russhwolf.settings.Settings
import com.ustadmobile.core.domain.language.SetLanguageUseCase
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
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

class AddAccountSelectNewOrExistingViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(AddAccountSelectNewOrExistingUiState())

    private val supportLangConfig: SupportedLanguagesConfig by instance()

    private val setLanguageUseCase: SetLanguageUseCase by instance()

    private val openExternalLinkUseCase: OpenExternalLinkUseCase by instance()

    private val settings: Settings by instance()

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
        navController.navigate(AddAccountSelectNewUserTypeViewModel.DEST_NAME, emptyMap())

    }
    fun onClickExistingUser(){
        navController.navigate(AddAccountExistingUserViewModel.DEST_NAME, emptyMap())

    }
    fun onClickBadgeQrCode(){

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