package com.ustadmobile.port.android.view.contententry.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.impl.ContainerStorageDir
import com.ustadmobile.core.impl.locale.entityconstants.LicenceConstants
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditUiState
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditViewModel
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.port.android.view.composable.*
import com.ustadmobile.core.R as CR


interface ContentEntryEdit2FragmentEventHandler {

    fun onClickUpdateContent()

    fun handleClickLanguage()

}

class ContentEntryEdit2Fragment : UstadBaseMvvmFragment(){



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {

                }
            }
        }
    }

}

@Composable
private fun ContentEntryEditScreen(
    viewModel: ContentEntryEditViewModel
) {
    val uiState by viewModel.uiState.collectAsState(ContentEntryEditUiState())
    ContentEntryEditScreen(
        uiState = uiState,
        onContentEntryChanged = viewModel::onContentEntryChanged
    )
}

@Composable
private fun ContentEntryEditScreen(
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
                stringResource(id = CR.string.file_required_prompt)
            else
                stringResource(id = CR.string.file_selected)

        if (uiState.updateContentVisible){

            Button(
                onClick = onClickUpdateContent,
                modifier = Modifier
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.secondaryColor)
                )
            ) {
                Text(stringResource(CR.string.update_content).uppercase(),
                    color = contentColorFor(
                        colorResource(id = R.color.secondaryColor))
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            Text(updateContentText)
        }

        Spacer(modifier = Modifier.height(15.dp))

        if (uiState.entity?.leaf == true){
            Text(text = stringResource(id = CR.string.supported_files))
        }

        Spacer(modifier = Modifier.height(15.dp))

        UstadTextEditField(
            value = uiState.entity?.title ?: "",
            label = stringResource(id = CR.string.title),
            error = uiState.titleError,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onContentEntryChanged(uiState.entity?.shallowCopy {
                        title = it
                    }
                )
            }
        )

        Spacer(modifier = Modifier.height(15.dp))

        UstadTextEditField(
            value = uiState.entity?.description ?: "",
            label = stringResource(id = CR.string.description),
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

        UstadTextEditField(
            value = uiState.entity?.author ?: "",
            label = stringResource(id = CR.string.entry_details_author),
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onContentEntryChanged(uiState.entity?.shallowCopy {
                        author = it
                    }
                )
            }
        )

        Spacer(modifier = Modifier.height(15.dp))

        UstadTextEditField(
            value = uiState.entity?.publisher ?: "",
            label = stringResource(id = CR.string.entry_details_publisher),
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
            label = stringResource(id = CR.string.licence),
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
                label = stringResource(CR.string.content_creation_storage_option_title),
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
                label = stringResource(id = CR.string.compress),
                enabled = uiState.fieldsEnabled,
                onChange = {
                    onChangeCompress(it)
                }
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        UstadSwitchField(
            checked = uiState.entity?.publik ?: false,
            label = stringResource(id = CR.string.publicly_accessible),
            enabled = uiState.fieldsEnabled,
            onChange = {
                onChangePubliclyAccessible(it)
            }
        )

        Spacer(modifier = Modifier.height(15.dp))

        UstadTextEditField(
            value = uiState.entity?.language?.name ?: "",
            label = stringResource(id = CR.string.language),
            readOnly = true,
            enabled = uiState.fieldsEnabled,
            onClick = onClickLanguage,
            onValueChange = {}
        )

        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
@Preview
fun ContentEntryEditScreenPreview() {
    val uiStateVal = ContentEntryEditUiState(
        entity = ContentEntryWithBlockAndLanguage().apply {
            leaf = true
        },
        updateContentVisible = true,
        metadataResult = MetadataResult(
            entry = ContentEntryWithLanguage(),
            importerId = 0
        ),
        courseBlockEditUiState = CourseBlockEditUiState(
            courseBlock = CourseBlock().apply {
                cbMaxPoints = 78
                cbCompletionCriteria = 14
            },
        ),
        storageOptions = listOf(
            ContainerStorageDir(
                name = "Device Memory",
                dirUri = ""
            ),
            ContainerStorageDir(
                name = "Memory Card",
                dirUri = ""
            ),
        ),
        selectedContainerStorageDir = ContainerStorageDir(
            name = "Device Memory",
            dirUri = ""
        )
    )

    MdcTheme {
        ContentEntryEditScreen(
            uiState = uiStateVal
        )
    }
}