package com.ustadmobile.libuicompose.view.person.accountedit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.person.accountedit.PersonAccountEditUiState
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.person.accountedit.PersonAccountEditViewModel
import com.ustadmobile.core.viewmodel.person.accountedit.PersonUsernameAndPasswordModel
import com.ustadmobile.libuicompose.components.UstadPasswordField
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun PersonAccountEditScreen(
    viewModel: PersonAccountEditViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(
        PersonAccountEditUiState(), Dispatchers.Main.immediate
    )

    PersonAccountEditScreen(
        uiState = uiState,
        onChange = viewModel::onEntityChanged
    )
}

@Composable
fun PersonAccountEditScreen(
    uiState: PersonAccountEditUiState,
    onChange: (PersonUsernameAndPasswordModel?) -> Unit = { },
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    )  {

        if (uiState.usernameVisible){
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().testTag("username"),
                value = uiState.personAccount?.username ?: "",
                maxLines = 1,
                label = {
                    Text(stringResource(MR.strings.username) + "*")
                },
                onValueChange = {
                    onChange(
                        uiState.personAccount?.copy(
                            username = it
                        )
                    )

                },
                isError = uiState.usernameError != null,
                enabled = uiState.fieldsEnabled,
                supportingText = {
                    Text(uiState.usernameError ?: stringResource(MR.strings.required))
                }
            )
        }



        if (uiState.currentPasswordVisible){
            UstadPasswordField(
                modifier = Modifier.fillMaxWidth().testTag("current_password"),
                value = uiState.personAccount?.currentPassword ?: "",
                label = {
                    Text(stringResource(MR.strings.current_password) + "*")
                },
                onValueChange = {
                    onChange(
                        uiState.personAccount?.copy(
                            currentPassword = it
                        )
                    )
                },
                isError = uiState.currentPasswordError != null,
                enabled = uiState.fieldsEnabled,
                supportingText = {
                    Text(uiState.currentPasswordError ?: stringResource(MR.strings.required))
                }
            )
        }

        UstadPasswordField(
            modifier = Modifier.fillMaxWidth().testTag("new_password"),
            value = uiState.personAccount?.newPassword ?: "",
            label = {
                Text(stringResource(MR.strings.new_password) + "*")
            },
            onValueChange = {
                onChange(
                    uiState.personAccount?.copy(
                        newPassword = it
                    )
                )
            },
            isError = uiState.newPasswordError != null,
            enabled = uiState.fieldsEnabled,
            supportingText = {
                Text(uiState.newPasswordError ?: stringResource(MR.strings.required))
            }
        )
    }
}
