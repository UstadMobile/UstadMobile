package com.ustadmobile.libuicompose.view.contententry.edit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.compress.CompressionLevel
import com.ustadmobile.core.impl.locale.entityconstants.LicenceConstants
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditUiState
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditViewModel
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadMessageIdOptionExposedDropDownMenuField
import com.ustadmobile.libuicompose.components.UstadRichTextEdit
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.core.viewmodel.contententry.stringResource
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun ContentEntryEditScreen(
    viewModel: ContentEntryEditViewModel
){
    val uiState by viewModel.uiState.collectAsState(ContentEntryEditUiState())

    ContentEntryEditScreen(
        uiState = uiState,
        onContentEntryChanged = viewModel::onContentEntryChanged,
        onClickEditDescription = viewModel::onEditDescriptionInNewWindow,
        onSetCompressionLevel = viewModel::onSetCompressionLevel,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentEntryEditScreen(
    uiState: ContentEntryEditUiState = ContentEntryEditUiState(),
    onCourseBlockChange: (CourseBlock?) -> Unit = {},
    onClickEditDescription: () -> Unit = { },
    onClickUpdateContent: () -> Unit = { },
    onContentEntryChanged: (ContentEntry?) -> Unit = {},
    onSetCompressionLevel: (CompressionLevel) -> Unit = { },
) {
    UstadVerticalScrollColumn(
        modifier = Modifier.fillMaxSize()
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
                    .defaultItemPadding()
                    .fillMaxWidth(),
            ) {
                Text(stringResource(MR.strings.update_content))
            }

            Text(
                modifier = Modifier.defaultItemPadding(),
                text = updateContentText
            )
        }

        OutlinedTextField(
            modifier = Modifier.testTag("title")
                .defaultItemPadding()
                .fillMaxWidth(),
            value = uiState.entity?.entry?.title ?: "",
            label = { Text(stringResource(MR.strings.title) + "*") },
            isError = uiState.titleError != null,
            enabled = uiState.fieldsEnabled,
            singleLine = true,
            onValueChange = {
                onContentEntryChanged(
                    uiState.entity?.entry?.shallowCopy {
                        title = it
                    }
                )
            },
            supportingText = {
                Text(uiState.titleError ?: stringResource(MR.strings.required))
            }
        )

        UstadRichTextEdit(
            html = uiState.entity?.entry?.description ?: "",
            modifier = Modifier.fillMaxWidth().defaultItemPadding()
                .testTag("description"),
            onHtmlChange = {
                onContentEntryChanged(
                    uiState.entity?.entry?.shallowCopy {
                        description = it
                    }
                )
            },
            onClickToEditInNewScreen = onClickEditDescription,
            editInNewScreenLabel = stringResource(MR.strings.description),
            placeholderText = stringResource(MR.strings.description),
        )

        OutlinedTextField(
            modifier = Modifier.testTag("author").fillMaxWidth().defaultItemPadding(),
            value = uiState.entity?.entry?.author ?: "",
            label = { Text(stringResource(MR.strings.entry_details_author)) },
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onContentEntryChanged(
                    uiState.entity?.entry?.shallowCopy {
                        author = it
                    }
                )
            }
        )

        OutlinedTextField(
            modifier = Modifier.testTag("publisher").fillMaxWidth().defaultItemPadding(),
            value = uiState.entity?.entry?.publisher ?: "",
            label = { Text(stringResource(MR.strings.entry_details_publisher)) },
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onContentEntryChanged(
                    uiState.entity?.entry?.shallowCopy {
                        publisher = it
                    }
                )
            }
        )

        UstadMessageIdOptionExposedDropDownMenuField(
            modifier = Modifier.testTag("license").defaultItemPadding(),
            value = uiState.entity?.entry?.licenseType ?: 0,
            options = LicenceConstants.LICENSE_MESSAGE_IDS,
            label = stringResource(MR.strings.licence),
            enabled = uiState.fieldsEnabled,
            onOptionSelected = {
                onContentEntryChanged(
                    uiState.entity?.entry?.shallowCopy {
                        licenseType = it.value
                    }
                )
            }
        )

        uiState.entity?.contentJobItem?.also { importJob ->
            // Dropdown as per
            // https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#ExposedDropdownMenuBox(kotlin.Boolean,kotlin.Function1,androidx.compose.ui.Modifier,kotlin.Function1)
            var expanded by remember { mutableStateOf(false) }
            val compressionLevel = CompressionLevel.forValue(importJob.cjiCompressionLevel)

            ExposedDropdownMenuBox(
                modifier = Modifier.fillMaxWidth().defaultItemPadding(),
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    readOnly = true,
                    enabled = uiState.fieldsEnabled,
                    value = stringResource(compressionLevel.stringResource),
                    label = {
                        Text(stringResource(MR.strings.compression))
                    },
                    onValueChange = { },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false}
                ) {
                    CompressionLevel.entries.forEach {
                        DropdownMenuItem(
                            text = { Text(stringResource(it.stringResource)) },
                            onClick = {
                                expanded = false
                                onSetCompressionLevel(it)
                            }
                        )
                    }
                }
            }
        }
    }
}