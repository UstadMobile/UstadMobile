package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.LanguageListView
import com.ustadmobile.core.view.SettingsView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.kodein.di.DI

data class SettingsUiState(

    val holidayCalendarVisible: Boolean = false,

    val workspaceSettingsVisible: Boolean = false,

    val reasonLeavingVisible: Boolean = false,

    val langListVisible: Boolean = false,
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

}