package com.ustadmobile.port.android.view.person.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.impl.locale.entityconstants.PersonConstants.GENDER_MESSAGE_IDS
import com.ustadmobile.core.viewmodel.person.edit.PersonEditUiState
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.port.android.view.composable.*
import com.ustadmobile.core.R as CR

class PersonEditFragment: UstadBaseMvvmFragment() {

    private val viewModel: PersonEditViewModel by ustadViewModels(::PersonEditViewModel)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    PersonEditScreen(viewModel)
                }
            }
        }
    }


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

        UstadInputFieldLayout (
            errorText = uiState.firstNameError
        ){
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = uiState.person?.firstNames ?: "",
                label = { Text(stringResource(id = CR.string.first_names)) },
                isError = uiState.firstNameError != null,
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onPersonChanged(uiState.person?.shallowCopy{
                        firstNames = it
                    })
                }
            )
        }

        UstadInputFieldLayout (
            errorText = uiState.lastNameError
        ){
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = uiState.person?.lastName ?: "",
                label = { Text(stringResource(id = CR.string.last_name)) },
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
        ) {
            UstadMessageIdOptionExposedDropDownMenuField(
                value = uiState.person?.gender ?: 0,
                modifier = Modifier
                    .testTag("gender")
                    .fillMaxWidth(),
                label = stringResource(CR.string.gender_literal),
                options = GENDER_MESSAGE_IDS,
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
            UstadTextEditField(
                value = uiState.approvalPersonParentJoin?.ppjEmail ?: "",
                label = stringResource(id = CR.string.parents_email_address),
                error = uiState.parentContactError,
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onApprovalPersonParentJoinChanged(
                        uiState.approvalPersonParentJoin?.shallowCopy {
                            ppjEmail = it
                        })
                }
            )
        }

        UstadInputFieldLayout(
            modifier = Modifier.fillMaxWidth(),
            errorText = uiState.dateOfBirthError
        ) {
            UstadDateField(
                value = uiState.person?.dateOfBirth ?: 0,
                label = { Text(stringResource(id = CR.string.birthday)) },
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


        UstadTextEditField(
            value = uiState.person?.phoneNum ?: "",
            label = stringResource(id = CR.string.phone_number),
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onPersonChanged(uiState.person?.shallowCopy{
                    phoneNum = it
                })
            }
        )

        UstadInputFieldLayout(
            errorText = uiState.emailError
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = uiState.person?.emailAddr ?: "",
                label = { Text(stringResource(id = CR.string.email)) },
                enabled = uiState.fieldsEnabled,
                isError = uiState.emailError != null,
                onValueChange = { newText ->
                    onPersonChanged(uiState.person?.shallowCopy {
                        emailAddr = newText
                    })
                }
            )
        }

        UstadTextEditField(
            value = uiState.person?.personAddress ?: "",
            label = stringResource(id = CR.string.address),
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onPersonChanged(uiState.person?.shallowCopy{
                    personAddress = it
                })
            }
        )

        if (uiState.usernameVisible){
            UstadInputFieldLayout (
                errorText = uiState.usernameError
            ){
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = uiState.person?.username ?: "",
                    label = { Text(stringResource(id = CR.string.username)) },
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
            UstadTextEditField(
                value = uiState.person?.newPassword ?: "",
                label = stringResource(id = CR.string.password),
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



@Composable
private fun PersonEditScreen(viewModel: PersonEditViewModel) {
    val uiState: PersonEditUiState by viewModel.uiState.collectAsState(PersonEditUiState())
    PersonEditScreen(
        uiState,
        onPersonChanged = viewModel::onEntityChanged,
        onApprovalPersonParentJoinChanged = viewModel::onApprovalPersonParentJoinChanged,
        onPersonPictureUriChanged = viewModel::onPersonPictureChanged,
    )
}

@Preview
@Composable
private fun PersonEditPreview() {
    val uiState = PersonEditUiState()
    PersonEditScreen(uiState)
}