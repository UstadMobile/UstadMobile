package com.ustadmobile.port.desktop.view.login

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.sun.jndi.toolkit.ctx.ComponentContext
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.ViewModel
import com.ustadmobile.core.viewmodel.login.LoginUiState
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import com.ustadmobile.libuicompose.view.login.LoginScreen

//As per https://github.com/JetBrains/compose-multiplatform-desktop-template#readme
fun main() = application {



    Window(
        onCloseRequest = ::exitApplication,
        title = "Compose for Desktop",
        state = rememberWindowState(width = 1024.dp, height = 768.dp)
    ) {


        val viewModel = rememberSaveableStateHolder { LoginViewModel() }

        MaterialTheme {
            LoginScreenForViewModel(viewModel)
        }
    }
}

@Composable
private fun LoginScreenForViewModel(
    viewModel: LoginViewModel
) {

    val uiState: LoginUiState by viewModel.states.collectAsState()

    LoginScreen(
        uiState = uiState,
        onClickLogin = viewModel::onClickLogin,
        onClickCreateAccount = viewModel::onClickCreateAccount,
        onClickConnectAsGuest = viewModel::handleConnectAsGuest,
        onUsernameValueChange = viewModel::onUsernameChanged,
        onPasswordValueChange = viewModel::onPasswordChanged,
    )
}


//@Composable
//@Preview
//fun LoginScreenPreview() {
//    MdcTheme {
//        LoginScreen()
//    }
//}