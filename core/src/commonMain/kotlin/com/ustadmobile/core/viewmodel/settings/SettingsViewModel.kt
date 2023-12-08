package com.ustadmobile.core.viewmodel.settings

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.language.SetLanguageUseCase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import org.kodein.di.instance

data class SettingsUiState(

    val holidayCalendarVisible: Boolean = false,

    val workspaceSettingsVisible: Boolean = false,

    val reasonLeavingVisible: Boolean = false,

    val langDialogVisible: Boolean = false,

    val currentLanguage: String = "",

    val availableLanguages: List<UstadMobileSystemCommon.UiLanguage> = emptyList(),

    val waitForRestartDialogVisible: Boolean = false,

)

class SettingsViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, DEST_NAME){

    private val _uiState = MutableStateFlow(SettingsUiState())

    val uiState: Flow<SettingsUiState> = _uiState.asStateFlow()

    private val supportedLangConfig: SupportedLanguagesConfig by instance()

    private val setLanguageUseCase: SetLanguageUseCase by instance()

    private val availableLangs = supportedLangConfig.supportedUiLanguagesAndSysDefault(systemImpl)

    init {
        _appUiState.update { prev ->
            prev.copy(title = systemImpl.getString(MR.strings.settings))
        }

        val langSetting = supportedLangConfig.localeSetting ?: UstadMobileSystemCommon.LOCALE_USE_SYSTEM

        val currentLang = availableLangs.first {
            it.langCode == langSetting
        }

        _uiState.update { prev ->
            prev.copy(
                currentLanguage = currentLang.langDisplay,
                availableLanguages = availableLangs
            )
        }

        viewModelScope.launch {
            activeRepo.scopedGrantDao.userHasAllPermissionsOnAllTablesGrant(activeUserPersonUid).collect { isAdmin ->
                _uiState.update { prev ->
                    prev.copy(workspaceSettingsVisible = isAdmin)
                }
            }
        }

    }

    fun onClickLanguage() {
        _uiState.update { prev ->
            prev.copy(
                langDialogVisible = true
            )
        }
    }

    fun onClickLang(lang: UstadMobileSystemCommon.UiLanguage) {
        _uiState.update { prev ->
            prev.copy(langDialogVisible = false)
        }

        val result = setLanguageUseCase(
            uiLang = lang,
            currentDestination = DEST_NAME,
            navController = navController
        )

        if(result.waitForRestart) {
            _uiState.update { prev ->
                prev.copy(
                    waitForRestartDialogVisible = true,
                )
            }
        }else {
            _uiState.update { prev ->
                prev.copy(
                    currentLanguage = lang.langDisplay
                )
            }
        }
    }

    fun onDismissLangDialog() {
        _uiState.update { prev ->
            prev.copy(langDialogVisible = false)
        }
    }

    fun onClickSiteSettings() {

    }


    companion object {
        const val DEST_NAME = "Settings"
    }


}
