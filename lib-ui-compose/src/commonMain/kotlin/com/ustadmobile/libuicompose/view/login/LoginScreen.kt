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
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.viewmodel.login.LoginUiState
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import com.ustadmobile.libuicompose.components.UstadPasswordField
import com.ustadmobile.libuicompose.locale.LocalStringResources
import com.ustadmobile.libuicompose.locale.localStringResources


@Composable
private fun LoginScreenForViewModel(
    viewModel: LoginViewModel
) {
    val uiState: LoginUiState by viewModel.uiState.collectAsState(LoginUiState())
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
private fun LoginScreen(
    uiState: LoginUiState = LoginUiState(),
    onClickLogin: () -> Unit = {},
    onClickCreateAccount: () -> Unit = {},
    onClickConnectAsGuest: () -> Unit = {},
    onUsernameValueChange: (String) -> Unit = {},
    onPasswordValueChange: (String) -> Unit = {},
) {
    val stringResources = localStringResources()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    )  {

        Text(text = uiState.loginIntentMessage ?: "")

        UstadInputFieldLayout(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            errorText = uiState.usernameError
        ) {
            OutlinedTextField(
                value = uiState.username,
                onValueChange = onUsernameValueChange,
                enabled = uiState.fieldsEnabled,
                isError = uiState.usernameError != null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
        }

        UstadInputFieldLayout(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            errorText = uiState.passwordError
        ) {
            UstadPasswordField(
                value = uiState.password,
                onValueChange = onPasswordValueChange,
                label = {
                    Text("Password")
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
            modifier = Modifier
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.secondary
            )
        ) {
            Text(stringResources[MessageID.login].uppercase(),
                color = contentColorFor(MaterialTheme.colors.secondary)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onClickCreateAccount,
            modifier = Modifier
                .fillMaxWidth(),
            enabled = uiState.fieldsEnabled,
        ) {
            Text(stringResources[MessageID.create_account].uppercase())
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onClickConnectAsGuest,
            modifier = Modifier
                .fillMaxWidth(),
            enabled = uiState.fieldsEnabled,
        ) {
            Text(stringResources[MessageID.connect_as_guest].uppercase())
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(uiState.versionInfo)
    }
}
