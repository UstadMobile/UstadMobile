package com.ustadmobile.libuicompose.view.signup

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.signup.SignupEnterUsernamePasswordUiState
import com.ustadmobile.core.viewmodel.signup.SignupEnterUsernamePasswordViewModel
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadPasswordField
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun SignUpEnterUsernamePasswordScreen(viewModel: SignupEnterUsernamePasswordViewModel) {
    val uiState: SignupEnterUsernamePasswordUiState by viewModel.uiState.collectAsStateWithLifecycle(
        SignupEnterUsernamePasswordUiState(), Dispatchers.Main.immediate
    )

    SignUpEnterUsernamePasswordScreen(
        uiState,
        onPersonChanged = viewModel::onEntityChanged,
        onClickedSignupEnterUsernamePassword = viewModel::onClickedSignupEnterUsernamePassword,
        onPasswordChanged = viewModel::onPasswordChanged,

        )

}

@Composable
fun SignUpEnterUsernamePasswordScreen(
    uiState: SignupEnterUsernamePasswordUiState = SignupEnterUsernamePasswordUiState(),
    onPersonChanged: (Person?) -> Unit = {},
    onClickedSignupEnterUsernamePassword: () -> Unit = {},
    onPasswordChanged: (String) -> Unit = { },

    ) {
    UstadVerticalScrollColumn(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            modifier = Modifier.testTag("username").fillMaxWidth().defaultItemPadding(),
            value = uiState.person?.username ?: "",
            label = { Text(stringResource(MR.strings.username)) },
            isError = uiState.usernameError != null,
            singleLine = true,
            onValueChange = {
                onPersonChanged(uiState.person?.shallowCopy {
                    username = it
                })
            },
            supportingText = {
                Text(uiState.usernameError ?: stringResource(MR.strings.required))
            }
        )


        UstadPasswordField(
            modifier = Modifier.testTag("password").fillMaxWidth().defaultItemPadding(),
            value = uiState.password ?: "",
            label = { Text(stringResource(MR.strings.password)) },
            isError = uiState.passwordError != null,

            onValueChange = {
                onPasswordChanged(it)
            },
            supportingText = {
                Text(uiState.passwordError ?: stringResource(MR.strings.required))
            }
        )

        Button(
            onClick = onClickedSignupEnterUsernamePassword,
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding()
                .testTag("signup_button"),
        ) {
            Text(

                stringResource(MR.strings.signup)

            )
        }


    }
}