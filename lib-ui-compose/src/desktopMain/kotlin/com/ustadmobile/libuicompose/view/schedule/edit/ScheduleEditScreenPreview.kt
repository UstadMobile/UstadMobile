package com.ustadmobile.libuicompose.view.schedule.edit

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.schedule.edit.ScheduleEditUiState

@Composable
@Preview
fun ScheduleEditScreenPreview() {
    ScheduleEditScreen(
        uiState = ScheduleEditUiState()
    )
}