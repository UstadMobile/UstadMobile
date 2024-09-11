package com.ustadmobile.libuicompose.view.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.login.LoginUiState
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import com.ustadmobile.libuicompose.components.PickFileOptions
import com.ustadmobile.libuicompose.components.PickType
import com.ustadmobile.libuicompose.components.UstadPasswordField
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import com.ustadmobile.libuicompose.components.rememberUstadFilePickLauncher
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
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
        onClickConnectAsGuest = viewModel::onClickConnectAsGuest,
        onUsernameValueChange = viewModel::onUsernameChanged,
        onPasswordValueChange = viewModel::onPasswordChanged,
        onSignInWithPasskey = viewModel::onSignInWithPassKey,
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
    onSignInWithPasskey: () -> Unit = {},
) {

    val filePickLauncher = rememberUstadFilePickLauncher { result ->

    }
    UstadVerticalScrollColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    )  {

        Text(text = uiState.loginIntentMessage ?: "")

        OutlinedTextField(
            modifier = Modifier
                .testTag("username")
                .defaultItemPadding()
                .fillMaxWidth(),
            value = uiState.username,
            singleLine = true,
            label = {
                Text(stringResource(MR.strings.username))
            },
            onValueChange = onUsernameValueChange,
            enabled = uiState.fieldsEnabled,
            isError = uiState.usernameError != null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            supportingText = uiState.usernameError?.let {
                { Text(it) }
            }
        )

        UstadPasswordField(
            modifier = Modifier
                .testTag("password")
                .defaultItemPadding()
                .fillMaxWidth()
            ,
            value = uiState.password,
            onValueChange = onPasswordValueChange,
            isError = uiState.passwordError != null,
            label = {
                Text(stringResource(MR.strings.password))
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { onClickLogin() }
            ),
            supportingText = uiState.passwordError?.let {
                { Text(it) }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text(text = uiState.errorMessage ?: "")

        Button(
            onClick = onClickLogin,
            enabled = uiState.fieldsEnabled,
            modifier = Modifier.testTag("login_button").fillMaxWidth().defaultItemPadding(),
        ) {
            Text(stringResource(MR.strings.login))
        }
        if(false) {
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { filePickLauncher(PickFileOptions(pickType = PickType.FILE)) },
                modifier = Modifier
                    .testTag("restore_local_account")
                    .defaultItemPadding()
                    .fillMaxWidth(),
            ) {
                Text(stringResource(MR.strings.restore_local_account_title))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        if(uiState.connectAsGuestVisible) {
            OutlinedButton(
                onClick = onClickConnectAsGuest,
                modifier = Modifier
                    .testTag("connect_as_guest_button")
                    .defaultItemPadding()
                    .fillMaxWidth(),
                enabled = uiState.fieldsEnabled,
            ) {
                Text(stringResource(MR.strings.connect_as_guest))
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Text(
            text = uiState.versionInfo,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.defaultItemPadding(),
        )

        val uriHandler = LocalUriHandler.current
        if(uiState.showPoweredBy) {
            Text(
                modifier = Modifier.defaultItemPadding().pointerHoverIcon(PointerIcon.Hand)
                    .clickable {
                        uriHandler.openUri("https://www.ustadmobile.com/")
                    },
                text = stringResource(MR.strings.powered_by),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Blue,
                textDecoration = TextDecoration.Underline,
            )

        }
    }
}
