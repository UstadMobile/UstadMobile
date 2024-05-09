package com.ustadmobile.libuicompose.view.person.edit

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.person.edit.PersonEditUiState
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadDateField
import com.ustadmobile.libuicompose.components.UstadImageSelectButton
import com.ustadmobile.libuicompose.components.UstadMessageIdOptionExposedDropDownMenuField
import com.ustadmobile.libuicompose.components.UstadPasswordField
import com.ustadmobile.libuicompose.components.UstadPhoneNumberTextField
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun PersonEditScreen(viewModel: PersonEditViewModel) {
    val uiState: PersonEditUiState by viewModel.uiState.collectAsStateWithLifecycle(
        PersonEditUiState(), Dispatchers.Main.immediate)

    PersonEditScreen(
        uiState,
        onPersonChanged = viewModel::onEntityChanged,
        onApprovalPersonParentJoinChanged = viewModel::onApprovalPersonParentJoinChanged,
        onPersonPictureUriChanged = viewModel::onPersonPictureChanged,
        onNationalNumberSetChanged = viewModel::onNationalPhoneNumSetChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
    )
}

@Composable
fun PersonEditScreen(
    uiState: PersonEditUiState = PersonEditUiState(),
    onPersonChanged: (Person?) -> Unit = {},
    onPasswordChanged: (String) -> Unit = { },
    onApprovalPersonParentJoinChanged: (PersonParentJoin?) -> Unit = {},
    onPersonPictureUriChanged: (String?) -> Unit = { },
    onNationalNumberSetChanged: (Boolean) -> Unit = { },
){
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
            label = { Text(stringResource(MR.strings.first_names)+"*") },
            isError = uiState.firstNameError != null,
            enabled = uiState.fieldsEnabled,
            singleLine = true,
            onValueChange = {
                onPersonChanged(uiState.person?.shallowCopy{
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
            label = { Text(stringResource(MR.strings.last_name) +"*") },
            isError = uiState.lastNameError != null,
            enabled = uiState.fieldsEnabled,
            singleLine = true,
            onValueChange = {
                onPersonChanged(uiState.person?.shallowCopy{
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
                onPersonChanged(uiState.person?.shallowCopy{
                    gender = it.value
                })
            },
            isError = uiState.genderError != null,
            enabled = uiState.fieldsEnabled,
            supportingText = {
                Text(uiState.genderError ?: stringResource(MR.strings.required))
            }
        )

        if (uiState.parentalEmailVisible){
            OutlinedTextField(
                modifier = Modifier.testTag("parents_email_address").fillMaxWidth().defaultItemPadding(),
                value = uiState.approvalPersonParentJoin?.ppjEmail ?: "",
                label = { Text(stringResource(MR.strings.parents_email_address)) },
                isError = uiState.parentContactError != null,
                singleLine = true,
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onApprovalPersonParentJoinChanged(
                        uiState.approvalPersonParentJoin?.shallowCopy {
                            ppjEmail = it
                        }
                    )
                },
                supportingText = {
                    Text(uiState.parentContactError ?: stringResource(MR.strings.required))
                }
            )
        }

        if(uiState.dateOfBirthVisible) {
            UstadDateField(
                modifier = Modifier.testTag("birthday").fillMaxWidth().defaultItemPadding(),
                value = uiState.person?.dateOfBirth ?: 0,
                label = { Text(stringResource(MR.strings.birthday)) },
                isError = uiState.dateOfBirthError != null,
                enabled = uiState.fieldsEnabled,
                timeZoneId = UstadMobileConstants.UTC,
                onValueChange = {
                    onPersonChanged(uiState.person?.shallowCopy{
                        dateOfBirth = it
                    })
                },
                supportingText = uiState.dateOfBirthError?.let {
                    { Text(it) }
                }
            )
        }

        if(uiState.phoneNumVisible) {
            UstadPhoneNumberTextField(
                value = uiState.person?.phoneNum ?: "",
                modifier = Modifier.testTag("phone_number").fillMaxWidth().defaultItemPadding(),
                label = { Text(stringResource(MR.strings.phone_number)) },
                onValueChange = {
                    onPersonChanged(
                        uiState.person?.shallowCopy{
                            phoneNum = it
                        }
                    )
                },
                onNationalNumberSetChanged = onNationalNumberSetChanged,
                isError = uiState.phoneNumError != null,
                supportingText = uiState.phoneNumError?.let {
                    { Text(it) }
                },
                countryCodeTestTag = "country_code",
                numberTextFieldTestTag = "phone_number_text"
            )
        }


        if(uiState.emailVisible) {
            OutlinedTextField(
                modifier = Modifier.testTag("email").fillMaxWidth().defaultItemPadding(),
                value = uiState.person?.emailAddr ?: "",
                label = { Text(stringResource(MR.strings.email)) },
                isError = uiState.emailError != null,
                singleLine = true,
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onPersonChanged(uiState.person?.shallowCopy{
                        emailAddr = it
                    })
                },
                supportingText = uiState.emailError?.let {
                    { Text(it) }
                }
            )
        }

        if(uiState.personAddressVisible) {
            OutlinedTextField(
                modifier = Modifier.testTag("address").fillMaxWidth().defaultItemPadding(),
                value = uiState.person?.personAddress ?: "",
                label = { Text(stringResource(MR.strings.address)) },
                enabled = uiState.fieldsEnabled,
                singleLine = true,
                onValueChange = {
                    onPersonChanged(uiState.person?.shallowCopy{
                        personAddress = it
                    })
                }
            )
        }


        if (uiState.usernameVisible){
            OutlinedTextField(
                modifier = Modifier.testTag("username").fillMaxWidth().defaultItemPadding(),
                value = uiState.person?.username ?: "",
                label = { Text(stringResource(MR.strings.username)) },
                enabled = uiState.fieldsEnabled,
                isError = uiState.usernameError != null,
                singleLine = true,
                onValueChange = {
                    onPersonChanged(uiState.person?.shallowCopy{
                        username = it
                    })
                },
                supportingText = {
                    Text(uiState.usernameError ?: stringResource(MR.strings.required))
                }
            )
        }

        if (uiState.passwordVisible){
            UstadPasswordField(
                modifier = Modifier.testTag("password").fillMaxWidth().defaultItemPadding(),
                value = uiState.password ?: "",
                label = { Text(stringResource(MR.strings.password)) },
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onPasswordChanged(it)
                },
                supportingText = {
                    Text(stringResource(MR.strings.required))
                }
            )
        }
    }
}