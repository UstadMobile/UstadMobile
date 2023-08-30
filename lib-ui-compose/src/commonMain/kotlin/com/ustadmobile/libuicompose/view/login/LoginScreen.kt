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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.login.LoginUiState
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.view.composable.UstadInputFieldLayout

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

        UstadInputFieldLayout(
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding(),
            errorText = uiState.usernameError,
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultItemPadding()
                    .testTag("username"),
                value = uiState.username,
                label = { Text("username") },
                onValueChange = {
                    onUsernameValueChange(it)
                },
                isError =  uiState.usernameError != null,
                enabled = uiState.fieldsEnabled,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
        }

        UstadInputFieldLayout(
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding(),
            errorText = uiState.passwordError,
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultItemPadding()
                    .testTag("password"),
                value = uiState.password,
                label = { Text("password") },
                onValueChange = {
                    onPasswordValueChange(it)
                },
                isError =  uiState.passwordError != null,
                enabled = uiState.fieldsEnabled,
                visualTransformation = PasswordVisualTransformation(),
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
            Text("login".uppercase(),
                color = contentColorFor(
                    MaterialTheme.colors.secondary)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onClickCreateAccount,
            modifier = Modifier
                .fillMaxWidth(),
            enabled = uiState.fieldsEnabled,
        ) {
            Text("create_account".uppercase())
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onClickConnectAsGuest,
            modifier = Modifier
                .fillMaxWidth(),
            enabled = uiState.fieldsEnabled,
        ) {
            Text("connect_as_guest".uppercase())
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(uiState.versionInfo)
    }
}
