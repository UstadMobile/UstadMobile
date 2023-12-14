package com.ustadmobile.libuicompose.view.settings

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.SettingsUiState


@Composable
@Preview
fun SettingsPreview() {
    val uiState = SettingsUiState(
        reasonLeavingVisible = true,
        holidayCalendarVisible = true,
        workspaceSettingsVisible = true,
        langListVisible = true
    )
    SettingsScreen(uiState)
}