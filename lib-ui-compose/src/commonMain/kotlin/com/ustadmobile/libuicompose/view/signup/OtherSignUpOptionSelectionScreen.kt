package com.ustadmobile.libuicompose.view.signup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.signup.OtherSignUpOptionSelectionUiState
import com.ustadmobile.core.viewmodel.signup.OtherSignUpOptionSelectionViewModel
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun OtherSignUpOptionSelectionScreen(viewModel: OtherSignUpOptionSelectionViewModel) {
    val uiState: OtherSignUpOptionSelectionUiState by viewModel.uiState.collectAsStateWithLifecycle(
        OtherSignUpOptionSelectionUiState(), Dispatchers.Main.immediate
    )

    OtherSignUpOptionSelectionScreen(
        uiState,
        onClickCreateLocalAccount = viewModel::onClickCreateLocalAccount,
        onclickSignUpWithPasskey = viewModel::onSignUpWithPasskey,
        onclickSignUpWithUsernameAdPassword = viewModel::onclickSignUpWithUsernameAdPassword
    )

}

@Composable
fun OtherSignUpOptionSelectionScreen(
    uiState: OtherSignUpOptionSelectionUiState = OtherSignUpOptionSelectionUiState(),
    onclickSignUpWithPasskey: () -> Unit = {},
    onClickCreateLocalAccount: () -> Unit = {},
    onclickSignUpWithUsernameAdPassword: () -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        LazyColumn {

            item {
                ListItem(
                    leadingContent = {},
                    headlineContent = {
                        Text(
                            text = stringResource(MR.strings.create_passkey_for_faster_and_easier_signin),
                        )
                    },
                    supportingContent = {
                        Text(
                            text = stringResource(MR.strings.with_passkeys_no_complex_passwords_needed),
                        )
                    },

                    )
            }
        }
        Button(
            onClick = onclickSignUpWithPasskey,
            modifier = Modifier.fillMaxWidth().defaultItemPadding()
        ) {
            Text(stringResource(MR.strings.signup_with_passkey))
        }

        OutlinedButton(
            onClick = onclickSignUpWithUsernameAdPassword,
            modifier = Modifier.fillMaxWidth().defaultItemPadding()
        ) {
            Text(stringResource(MR.strings.create_username_and_password))
        }
        if (uiState.showCreateLocaleAccount) {
            OutlinedButton(
                onClick = onClickCreateLocalAccount,
                modifier = Modifier.fillMaxWidth().defaultItemPadding()
            ) {
                Text(stringResource(MR.strings.create_local_account))
            }
        }
    }
}