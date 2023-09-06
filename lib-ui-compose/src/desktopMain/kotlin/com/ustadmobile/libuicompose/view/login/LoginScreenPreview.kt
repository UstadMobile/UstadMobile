package com.ustadmobile.libuicompose.view.login

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.login.LoginUiState


@Composable
@Preview
fun LoginScreenPreview() {
    LoginScreen(
        uiState = LoginUiState()
    )
}
