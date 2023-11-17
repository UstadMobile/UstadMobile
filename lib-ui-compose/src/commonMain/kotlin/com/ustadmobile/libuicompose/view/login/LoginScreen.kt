package com.ustadmobile.libuicompose.view.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.login.LoginUiState
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import com.ustadmobile.libuicompose.components.UstadPasswordField
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.util.ext.onPreviewKeyEventFocusHandler
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun LoginScreen(
    viewModel: LoginViewModel
) {
    val uiState: LoginUiState by viewModel.uiState.collectAsStateWithLifecycle(LoginUiState(),
        Dispatchers.Main.immediate)

    LoginScreen(
        uiState = uiState,
        onClickLogin = viewModel::onClickLogin,
        onClickCreateAccount = viewModel::onClickCreateAccount,
        onClickConnectAsGuest = viewModel::handleConnectAsGuest,
        onUsernameValueChange = viewModel::onUsernameChanged,
        onPasswordValueChange = viewModel::onPasswordChanged,
    )
}

@Composable
fun LoginScreen(
    uiState: LoginUiState = LoginUiState(),
    onClickLogin: () -> Unit = {},
    onClickCreateAccount: () -> Unit = {},
    onClickConnectAsGuest: () -> Unit = {},
    onUsernameValueChange: (String) -> Unit = {},
    onPasswordValueChange: (String) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    )  {

        Text(text = uiState.loginIntentMessage ?: "")

        val focusManager = LocalFocusManager.current

        UstadInputFieldLayout(
            modifier = Modifier.padding(vertical = 8.dp)
                .fillMaxWidth(),
            errorText = uiState.usernameError
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .testTag("username")
                    .fillMaxWidth()
                    .onPreviewKeyEventFocusHandler(focusManager),
                value = uiState.username,
                singleLine = true,
                label = {
                    Text(stringResource(MR.strings.username))
                },
                onValueChange = onUsernameValueChange,
                enabled = uiState.fieldsEnabled,
                isError = uiState.usernameError != null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
        }

        UstadInputFieldLayout(
            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
            errorText = uiState.passwordError
        ) {
            UstadPasswordField(
                modifier = Modifier
                    .testTag("password")
                    .fillMaxWidth()
                    .onPreviewKeyEventFocusHandler(focusManager),
                value = uiState.password,
                onValueChange = onPasswordValueChange,
                isError = uiState.passwordError != null,
                label = {
                    Text(stringResource(MR.strings.password))
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { onClickLogin() }
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = uiState.errorMessage ?: "")

        Button(
            onClick = onClickLogin,
            enabled = uiState.fieldsEnabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(MR.strings.login))
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onClickCreateAccount,
            modifier = Modifier
                .fillMaxWidth(),
            enabled = uiState.fieldsEnabled,
        ) {
            Text(stringResource(MR.strings.create_account))
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onClickConnectAsGuest,
            modifier = Modifier
                .fillMaxWidth(),
            enabled = uiState.fieldsEnabled,
        ) {
            Text(stringResource(MR.strings.connect_as_guest))
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(uiState.versionInfo)
    }
}
