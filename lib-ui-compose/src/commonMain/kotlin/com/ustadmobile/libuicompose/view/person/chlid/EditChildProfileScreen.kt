package com.ustadmobile.libuicompose.view.person.chlid

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.viewmodel.person.child.EditChildProfileUiState
import com.ustadmobile.core.viewmodel.person.child.EditChildProfileViewModel
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadDateField
import com.ustadmobile.libuicompose.components.UstadMessageIdOptionExposedDropDownMenuField
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun EditChildProfileScreen(viewModel: EditChildProfileViewModel) {
    val uiState: EditChildProfileUiState by viewModel.uiState.collectAsStateWithLifecycle(
        EditChildProfileUiState(), Dispatchers.Main.immediate)
    EditChildProfileScreen(
        uiState = uiState,
        onPersonChanged = viewModel::onEntityChanged,
    )
}

@Composable
fun EditChildProfileScreen(
    uiState: EditChildProfileUiState,
    onPersonChanged: (Person?) -> Unit = {},
) {
    UstadVerticalScrollColumn(
        modifier = Modifier.fillMaxSize()
    )  {

        OutlinedTextField(
            modifier = Modifier.testTag("first_names").fillMaxWidth()
                .defaultItemPadding(),
            value = uiState.person?.firstNames ?: "",
            label = { Text(stringResource(MR.strings.first_names) +"*") },
            isError = uiState.firstNameError != null,
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
            supportingText = {
                Text(uiState.genderError ?: stringResource(MR.strings.required))
            }
        )


            UstadDateField(
                modifier = Modifier.testTag("birthday").fillMaxWidth().defaultItemPadding(),
                value = uiState.person?.dateOfBirth ?: 0,
                label = { Text(stringResource(MR.strings.birthday)) },
                isError = uiState.dateOfBirthError != null,
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

}