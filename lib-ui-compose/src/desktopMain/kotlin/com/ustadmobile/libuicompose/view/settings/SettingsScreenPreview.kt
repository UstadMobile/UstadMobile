package com.ustadmobile.libuicompose.view.settings

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.settings.SettingsUiState

@Preview
@Composable
fun SettingsPreview() {
    val uiState = SettingsUiState(
        reasonLeavingVisible = true,
        holidayCalendarVisible = true,
        workspaceSettingsVisible = true,
        langDialogVisible = true,
        sendAppOptionVisible = true
    )
    SettingsScreen(
        uiState = uiState,
        onClickAppLanguage = { },
        onClickWorkspace = { },
        onClickHtmlContentDisplayEngine = { },
        onClickVersion = { },
        onClickDeveloperOptions = { },
        onClickDeletedItems = { },
        onClickOfflineStorageOptionsDialog = { },
        onClickCreateBackup = { },
        folderPickLauncher = { },
        onClickAppShare = { }
    )
}