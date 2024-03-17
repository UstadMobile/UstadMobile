package com.ustadmobile.libuicompose.view.person.bulkaddselectfile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.viewmodel.person.bulkaddselectfile.BulkAddPersonSelectFileUiState
import com.ustadmobile.core.viewmodel.person.bulkaddselectfile.BulkAddPersonSelectFileViewModel
import com.ustadmobile.libuicompose.components.UstadFileDropZone
import com.ustadmobile.libuicompose.components.UstadPickFileOpts
import com.ustadmobile.libuicompose.components.rememberUstadFilePickLauncher
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding

@Composable
fun BulkAddPersonSelectFileScreen(viewModel: BulkAddPersonSelectFileViewModel) {
    val uiState by viewModel.uiState.collectAsState(
        BulkAddPersonSelectFileUiState()
    )

    val filePickLauncher = rememberUstadFilePickLauncher {
        viewModel.onFileSelected(uri = it.uri, name = it.fileName)
    }

    BulkAddPersonSelectFileScreen(
        uiState = uiState,
        onClickSelectFile = {
            filePickLauncher(UstadPickFileOpts())
        },
        onFileSelected = viewModel::onFileSelected,
        onClickImportButton = viewModel::onClickImportButton
    )
}

@Composable
fun BulkAddPersonSelectFileScreen(
    uiState: BulkAddPersonSelectFileUiState,
    onClickSelectFile: () -> Unit,
    onFileSelected: (uri: String, name: String) -> Unit,
    onClickImportButton: () -> Unit,
) {

    Column {
        Text(
            text = "Instructions...",
            modifier = Modifier.defaultItemPadding(),
        )

        UstadFileDropZone(
            onFileDropped = {
                onFileSelected(it.uri, it.fileName)
            }
        ) {
            Column {
                ListItem(
                    leadingContent = {
                        Icon(Icons.Default.InsertDriveFile, contentDescription = null)
                    },
                    headlineContent = {
                        Text(uiState.selectedFileName ?: stringResource(MR.strings.none_key))
                    },
                    supportingContent = {
                        Text(stringResource(MR.strings.file_selected))
                    },
                )

                OutlinedButton(
                    onClick = onClickSelectFile,
                    modifier = Modifier.defaultItemPadding().fillMaxWidth(),
                ) {
                    Text("Select file")
                }

                Button(
                    onClick = onClickImportButton,
                    enabled = uiState.importButtonEnabled,
                    modifier = Modifier.defaultItemPadding().fillMaxWidth(),
                ) {
                    Text(stringResource(MR.strings.import))
                }
            }
        }

    }
}


