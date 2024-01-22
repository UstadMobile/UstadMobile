package com.ustadmobile.libuicompose.view.person.edit

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.person.edit.PersonEditUiState

@Preview
@Composable
private fun PersonEditPreview() {
    PersonEditScreen(uiState = PersonEditUiState())
}