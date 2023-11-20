package com.ustadmobile.libuicompose.view.person.registerminorwaitforparent

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.RegisterMinorWaitForParentUiState


@Composable
@Preview
fun  RegisterMinorWaitForParentScreenPreview() {
    val uiStateVal = RegisterMinorWaitForParentUiState(
        username = "new.username",
        password = "secret",
        parentContact = "parent@email.com"
    )

    RegisterMinorWaitForParentScreen(
        uiState = uiStateVal,
    )

}