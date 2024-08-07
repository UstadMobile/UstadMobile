package com.ustadmobile.libuicompose.view.signup

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.passkey.PasskeyData
import com.ustadmobile.core.viewmodel.signup.SignUpUiState
import com.ustadmobile.core.viewmodel.signup.SignUpViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadImageSelectButton
import com.ustadmobile.libuicompose.components.UstadMessageIdOptionExposedDropDownMenuField
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.passkey.CreatePasskeyPrompt
import dev.icerock.moko.resources.compose.stringResource
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun SignUpScreen(viewModel: SignUpViewModel) {
    val uiState: SignUpUiState by viewModel.uiState.collectAsStateWithLifecycle(
        SignUpUiState(), Dispatchers.Main.immediate
    )

    SignUpScreen(
        uiState,
        onPersonChanged = viewModel::onEntityChanged,
        onPersonPictureUriChanged = viewModel::onPersonPictureChanged,
        onTeacherCheckChanged = viewModel::onTeacherCheckChanged,
        onParentCheckChanged = viewModel::onParentCheckChanged,
        onclickSignUpWithPasskey = viewModel::onSignUpWithPasskey,
        onPassKeyDataReceived = viewModel::onPassKeyDataReceived,
        onPassKeyError = viewModel::onPassKeyError,
    )

}

@Composable
fun SignUpScreen(
    uiState: SignUpUiState = SignUpUiState(),
    onPersonChanged: (Person?) -> Unit = {},
    onclickSignUpWithPasskey: () -> Unit = {},
    onclickOtherOptions: () -> Unit = {},
    onPersonPictureUriChanged: (String?) -> Unit = { },
    onTeacherCheckChanged: (Boolean) -> Unit = { },
    onParentCheckChanged: (Boolean) -> Unit = { },
    onPassKeyDataReceived: (PasskeyData) -> Unit = { },
    onPassKeyError: (String) -> Unit = { },
) {
    UstadVerticalScrollColumn(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        UstadImageSelectButton(
            imageUri = uiState.personPicture?.personPictureUri,
            onImageUriChanged = onPersonPictureUriChanged,
            modifier = Modifier.size(60.dp),
        )

        OutlinedTextField(
            modifier = Modifier.testTag("first_names").fillMaxWidth()
                .defaultItemPadding(),
            value = uiState.person?.firstNames ?: "",
            label = { Text(stringResource(MR.strings.first_names) + "*") },
            isError = uiState.firstNameError != null,
            singleLine = true,
            onValueChange = {
                onPersonChanged(uiState.person?.shallowCopy {
                    firstNames = it
                })
            },
            supportingText = {
                Text(uiState.firstNameError ?: stringResource(MR.strings.required))
            }
        )

        OutlinedTextField(
            modifier = Modifier.testTag("last_name").fillMaxWidth()
                .defaultItemPadding(),
            value = uiState.person?.lastName ?: "",
            label = { Text(stringResource(MR.strings.last_name) + "*") },
            isError = uiState.lastNameError != null,
            singleLine = true,
            onValueChange = {
                onPersonChanged(uiState.person?.shallowCopy {
                    lastName = it
                })
            },
            supportingText = {
                Text(uiState.lastNameError ?: stringResource(MR.strings.required))
            }
        )

        UstadMessageIdOptionExposedDropDownMenuField(
            value = uiState.person?.gender ?: 0,
            modifier = Modifier
                .testTag("gender")
                .defaultItemPadding()
                .fillMaxWidth(),
            label = stringResource(MR.strings.gender_literal) + "*",
            options = uiState.genderOptions.filter { it.stringResource != MR.strings.blank },
            onOptionSelected = {
                onPersonChanged(uiState.person?.shallowCopy {
                    gender = it.value
                })
            },
            isError = uiState.genderError != null,
            supportingText = {
                Text(uiState.genderError ?: stringResource(MR.strings.required))
            }
        )
        Row(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = uiState.isTeacher ?: false,
                onCheckedChange = { onTeacherCheckChanged(it) }
            )
            Text(
                text = stringResource(MR.strings.i_am_teacher),
                modifier = Modifier.padding(start = 4.dp, end = 16.dp)
            )

            Checkbox(
                checked = uiState.isParent ?: false,
                onCheckedChange = { onParentCheckChanged(it) }
            )
            Text(
                text = stringResource(MR.strings.i_am_parent),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        Button(
            onClick = onclickSignUpWithPasskey,
            modifier = Modifier.fillMaxWidth().defaultItemPadding()
                .testTag("signup_passkey_button"),
        ) {
            Text(stringResource(MR.strings.signup_with_passkey))
        }
        OutlinedButton(
            onClick = onclickOtherOptions,
            modifier = Modifier.fillMaxWidth().defaultItemPadding().testTag("other_option_button"),
        ) {
            Text(stringResource(MR.strings.other_options))
        }

        uiState.showCreatePasskeyPrompt?.also { showPrompt ->
            if (showPrompt) {
                uiState.serverUrl_?.let {serverUrl->
                    CreatePasskeyPrompt(
                        username = uiState.person?.username ?: uiState.person?.firstNames?:"",
                        personUid = uiState.person?.personUid.toString(),
                        doorNodeId = uiState.doorNodeId.toString(),
                        usStartTime = systemTimeInMillis(),
                        serverUrl = serverUrl,
                        passkeyData = {
                            onPassKeyDataReceived(it,)
                        },
                        passkeyError = {
                            onPassKeyError(it)
                        }
                    )
                }

            }
        }


    }
}