package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.*
import com.ustadmobile.core.viewmodel.login.LoginUiState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.kodein.di.DI

data class SettingsUiState(

    val holidayCalendarVisible: Boolean = true,

    val workspaceSettingsVisible: Boolean = true,

    val reasonLeavingVisible: Boolean = true,

    val langListVisible: Boolean = true,
)

class SettingsViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
) : UstadViewModel(di, savedStateHandle, SettingsView.VIEW_NAME) {

    private val _uiState = MutableStateFlow(SettingsUiState())

    val uiState: Flow<SettingsUiState> = _uiState.asStateFlow()

    init {

    }

    fun onClickAppLanguage() {
        val viewName = LanguageListView.VIEW_NAME
        navController.navigate(viewName, emptyMap())
    }

    fun onClickGoToHolidayCalendarList() {
        navController.navigate(HolidayCalendarListView.VIEW_NAME, emptyMap())
    }

    fun onClickWorkspace() {
        navController.navigate(SiteDetailView.VIEW_NAME, emptyMap())
    }

    fun onClickLeavingReason() {
        navController.navigate(LeavingReasonListView.VIEW_NAME, emptyMap())
    }

    fun onClickLangList(){
        navController.navigate(LanguageEditView.VIEW_NAME, emptyMap())
    }
}