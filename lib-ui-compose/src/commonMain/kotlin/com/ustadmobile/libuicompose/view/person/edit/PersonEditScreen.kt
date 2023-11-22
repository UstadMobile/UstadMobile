package com.ustadmobile.libuicompose.view.person.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.person.edit.PersonEditUiState
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.impl.locale.entityconstants.PersonConstants
import com.ustadmobile.libuicompose.components.UstadDateField
import com.ustadmobile.libuicompose.components.UstadImageSelectButton
import com.ustadmobile.libuicompose.components.UstadMessageIdOptionExposedDropDownMenuField
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun PersonEditScreenForViewModel(viewModel: PersonEditViewModel) {
    val uiState: PersonEditUiState by viewModel.uiState.collectAsState(PersonEditUiState())
    PersonEditScreen(
        uiState,
        onPersonChanged = viewModel::onEntityChanged,
        onApprovalPersonParentJoinChanged = viewModel::onApprovalPersonParentJoinChanged,
        onPersonPictureUriChanged = viewModel::onPersonPictureChanged,
    )
}

@Composable
fun PersonEditScreen(
    uiState: PersonEditUiState = PersonEditUiState(),
    onPersonChanged: (PersonWithAccount?) -> Unit = {},
    onApprovalPersonParentJoinChanged: (PersonParentJoin?) -> Unit = {},
    onPersonPictureUriChanged: (String?) -> Unit = { }
){
    Column(
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        UstadImageSelectButton(
            imageUri = uiState.personPicture?.personPictureUri,
            onImageUriChanged = onPersonPictureUriChanged,
            modifier = Modifier.size(60.dp),
        )

        UstadInputFieldLayout(
            modifier = Modifier.padding(vertical = 8.dp)
                .fillMaxWidth(),
            errorText = uiState.firstNameError,
            captionIfNoError = {
                Text(stringResource(MR.strings.required))
            }
        ) {
            OutlinedTextField(
                modifier = Modifier.testTag("firstNames").fillMaxWidth(),
                value = uiState.person?.firstNames ?: "",
                label = { Text(stringResource(MR.strings.first_names)) },
                isError = uiState.firstNameError != null,
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onPersonChanged(uiState.person?.shallowCopy{
                        firstNames = it
                    })
                }
            )
        }

        UstadInputFieldLayout(
            modifier = Modifier.padding(vertical = 8.dp)
                .fillMaxWidth(),
            errorText = uiState.lastNameError,
            captionIfNoError = {
                Text(stringResource(MR.strings.required))
            }
        ) {
            OutlinedTextField(
                modifier = Modifier.testTag("lastName").fillMaxWidth(),
                value = uiState.person?.lastName ?: "",
                label = { Text(stringResource(MR.strings.last_name)) },
                isError = uiState.lastNameError != null,
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onPersonChanged(uiState.person?.shallowCopy{
                        lastName = it
                    })
                }
            )
        }


        UstadInputFieldLayout(
            modifier = Modifier.fillMaxWidth(),
            errorText = uiState.genderError,
            captionIfNoError = {
                Text(stringResource(MR.strings.required))
            }
        ) {
            UstadMessageIdOptionExposedDropDownMenuField(
                value = uiState.person?.gender ?: 0,
                modifier = Modifier
                    .testTag("gender")
                    .fillMaxWidth(),
                label = stringResource(MR.strings.gender_literal),
                options = PersonConstants.GENDER_MESSAGE_IDS,
                onOptionSelected = {
                    onPersonChanged(uiState.person?.shallowCopy{
                        gender = it.value
                    })
                },
                isError = uiState.genderError != null,
                enabled = uiState.fieldsEnabled,
            )
        }


        if (uiState.parentalEmailVisible){
            UstadInputFieldLayout(
                modifier = Modifier.padding(vertical = 8.dp)
                    .fillMaxWidth(),
                errorText = uiState.parentContactError
            ) {
                OutlinedTextField(
                    modifier = Modifier.testTag("ppjEmail").fillMaxWidth(),
                    value = uiState.approvalPersonParentJoin?.ppjEmail ?: "",
                    label = { Text(stringResource(MR.strings.parents_email_address)) },
                    isError = uiState.parentContactError != null,
                    enabled = uiState.fieldsEnabled,
                    onValueChange = {
                        onApprovalPersonParentJoinChanged(
                            uiState.approvalPersonParentJoin?.shallowCopy {
                                ppjEmail = it
                            })
                    }
                )
            }
        }

        UstadInputFieldLayout(
            modifier = Modifier.fillMaxWidth(),
            errorText = uiState.dateOfBirthError
        ) {
            UstadDateField(
                modifier = Modifier.testTag("dateOfBirth").fillMaxWidth(),
                value = uiState.person?.dateOfBirth ?: 0,
                label = { Text(stringResource(MR.strings.birthday)) },
                isError = uiState.dateOfBirthError != null,
                enabled = uiState.fieldsEnabled,
                timeZoneId = UstadMobileConstants.UTC,
                onValueChange = {
                    onPersonChanged(uiState.person?.shallowCopy{
                        dateOfBirth = it
                    })
                }
            )
        }


        OutlinedTextField(
            modifier = Modifier.testTag("phoneNum").fillMaxWidth(),
            value = uiState.person?.phoneNum ?: "",
            label = { Text(stringResource(MR.strings.phone_number)) },
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onPersonChanged(uiState.person?.shallowCopy{
                    phoneNum = it
                })
            }
        )

        UstadInputFieldLayout(
            modifier = Modifier.fillMaxWidth(),
            errorText = uiState.emailError
        ) {
            OutlinedTextField(
                modifier = Modifier.testTag("emailAddr").fillMaxWidth(),
                value = uiState.person?.emailAddr ?: "",
                label = { Text(stringResource(MR.strings.email)) },
                isError = uiState.emailError != null,
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onPersonChanged(uiState.person?.shallowCopy{
                        emailAddr = it
                    })
                }
            )

        }

        OutlinedTextField(
            modifier = Modifier.testTag("personAddress").fillMaxWidth(),
            value = uiState.person?.personAddress ?: "",
            label = { Text(stringResource(MR.strings.address)) },
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onPersonChanged(uiState.person?.shallowCopy{
                    personAddress = it
                })
            }
        )

        if (uiState.usernameVisible){
            UstadInputFieldLayout (
                errorText = uiState.usernameError,
                captionIfNoError = {
                    Text(stringResource(MR.strings.required))
                }
            ){
                OutlinedTextField(
                    modifier = Modifier.testTag("username").fillMaxWidth(),
                    value = uiState.person?.username ?: "",
                    label = { Text(stringResource(MR.strings.username)) },
                    enabled = uiState.fieldsEnabled,
                    isError = uiState.usernameError != null,
                    onValueChange = {
                        onPersonChanged(uiState.person?.shallowCopy{
                            username = it
                        })
                    }
                )
            }
        }

        if (uiState.passwordVisible){
            OutlinedTextField(
                modifier = Modifier.testTag("newPassword").fillMaxWidth(),
                value = uiState.person?.newPassword ?: "",
                label = { Text(stringResource(MR.strings.password)) },
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onPersonChanged(uiState.person?.shallowCopy{
                        newPassword = it
                    })
                }
            )
        }
    }
}