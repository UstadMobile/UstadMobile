package com.ustadmobile.libuicompose.view.contententry.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.impl.ContainerStorageDir
import com.ustadmobile.core.impl.locale.entityconstants.LicenceConstants
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditUiState
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditViewModel
import com.ustadmobile.core.MR
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadCourseBlockEdit
import com.ustadmobile.libuicompose.components.UstadExposedDropDownMenuField
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import com.ustadmobile.libuicompose.components.UstadMessageIdOptionExposedDropDownMenuField
import com.ustadmobile.libuicompose.components.UstadSwitchField
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun ContentEntryEditScreenForViewModel(
    viewModel: ContentEntryEditViewModel
){
    val uiState by viewModel.uiState.collectAsState(ContentEntryEditUiState())

    ContentEntryEditScreen(
        uiState = uiState,
        onContentEntryChanged = viewModel::onContentEntryChanged
    )
}

@Composable
fun ContentEntryEditScreen(
    uiState: ContentEntryEditUiState = ContentEntryEditUiState(),
    onCourseBlockChange: (CourseBlock?) -> Unit = {},
    onClickUpdateContent: () -> Unit = {},
    onContentEntryChanged: (ContentEntry?) -> Unit = {},
    onChangeCompress: (Boolean) -> Unit = {},
    onChangePubliclyAccessible: (Boolean) -> Unit = {},
    onClickLanguage: () -> Unit = {},
    onSelectContainerStorageDir: (ContainerStorageDir) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    )  {

        val updateContentText =
            if (!uiState.importError.isNullOrBlank())
                stringResource(MR.strings.file_required_prompt)
            else
                stringResource(MR.strings.file_selected)

        if (uiState.updateContentVisible){

            Button(
                onClick = onClickUpdateContent,
                modifier = Modifier
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
//                    backgroundColor = colorResource(id = R.color.secondaryColor)
                )
            ) {
                Text(stringResource(MR.strings.update_content).uppercase(),
//                    color = contentColorFor(
//                        colorResource(id = R.color.secondaryColor))
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            Text(updateContentText)
        }

        Spacer(modifier = Modifier.height(15.dp))

        if (uiState.entity?.leaf == true){
            Text(text = stringResource(MR.strings.supported_files))
        }

        Spacer(modifier = Modifier.height(15.dp))

        UstadInputFieldLayout(
            modifier = Modifier.fillMaxWidth(),
            errorText = uiState.titleError
        ) {
            OutlinedTextField(
                modifier = Modifier.testTag("title").fillMaxWidth(),
                value = uiState.entity?.title ?: "",
                label = { Text(stringResource(MR.strings.title)) },
                isError = uiState.titleError != null,
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onContentEntryChanged(uiState.entity?.shallowCopy {
                        title = it
                    }
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        OutlinedTextField(
            modifier = Modifier.testTag("description").fillMaxWidth(),
            value = uiState.entity?.description ?: "",
            label = { Text(stringResource(MR.strings.description)) },
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onContentEntryChanged(uiState.entity?.shallowCopy {
                    description = it
                }
                )
            }
        )

        Spacer(modifier = Modifier.height(15.dp))

        if(uiState.courseBlockVisible) {
            UstadCourseBlockEdit(
                uiState = uiState.courseBlockEditUiState,
                onCourseBlockChange = onCourseBlockChange
            )
        }


        Spacer(modifier = Modifier.height(15.dp))

        OutlinedTextField(
            modifier = Modifier.testTag("author").fillMaxWidth(),
            value = uiState.entity?.author ?: "",
            label = { Text(stringResource(MR.strings.entry_details_author)) },
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onContentEntryChanged(uiState.entity?.shallowCopy {
                    author = it
                }
                )
            }
        )

        Spacer(modifier = Modifier.height(15.dp))

        OutlinedTextField(
            modifier = Modifier.testTag("publisher").fillMaxWidth(),
            value = uiState.entity?.publisher ?: "",
            label = { Text(stringResource(MR.strings.entry_details_publisher)) },
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onContentEntryChanged(uiState.entity?.shallowCopy {
                    publisher = it
                }
                )
            }
        )

        Spacer(modifier = Modifier.height(15.dp))

        UstadMessageIdOptionExposedDropDownMenuField(
            value = uiState.entity?.licenseType ?: 0,
            options = LicenceConstants.LICENSE_MESSAGE_IDS,
            label = stringResource(MR.strings.licence),
            enabled = uiState.fieldsEnabled,
            onOptionSelected = {
                onContentEntryChanged(uiState.entity?.shallowCopy {
                    licenseType = it.value
                }
                )
            }
        )

        Spacer(modifier = Modifier.height(15.dp))

        if (uiState.containerStorageOptionVisible){
            UstadExposedDropDownMenuField(
                value = uiState.selectedContainerStorageDir,
                label = stringResource(MR.strings.content_creation_storage_option_title),
                options = uiState.storageOptions,
                onOptionSelected = { onSelectContainerStorageDir(it as ContainerStorageDir) },
                itemText = { (it as ContainerStorageDir).name ?: "" },
                enabled = uiState.fieldsEnabled,
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        if (uiState.contentCompressVisible){
            UstadSwitchField(
                checked = uiState.compressionEnabled,
                label = stringResource(MR.strings.compress),
                enabled = uiState.fieldsEnabled,
                onChange = {
                    onChangeCompress(it)
                }
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        UstadSwitchField(
            checked = uiState.entity?.publik ?: false,
            label = stringResource(MR.strings.publicly_accessible),
            enabled = uiState.fieldsEnabled,
            onChange = {
                onChangePubliclyAccessible(it)
            }
        )

        Spacer(modifier = Modifier.height(15.dp))

        OutlinedTextField(
            modifier = Modifier.testTag("languageName").fillMaxWidth(),
            value = uiState.entity?.language?.name ?: "",
            label = { Text(stringResource(MR.strings.language)) },
//            readOnly = true,
//            enabled = uiState.fieldsEnabled,
//            onClick = onClickLanguage,
            onValueChange = {}
        )

        Spacer(modifier = Modifier.height(64.dp))
    }
}