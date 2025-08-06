package com.ustadmobile.libuicompose.view.settings.storageanddata

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConnectWithoutContact
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.viewmodel.settings.storageanddata.StorageAndDataSettingsViewModel
import com.ustadmobile.core.viewmodel.settings.storageanddata.StorageAndSettingsUiState
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import com.ustadmobile.core.MR
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.libuicompose.components.UstadDetailField2
import com.ustadmobile.libuicompose.view.settings.SettingsDialog
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun StorageAndDataSettingsScreen(
    viewModel: StorageAndDataSettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(StorageAndSettingsUiState())

    StorageAndDataSettingsScreen(
        uiState = uiState,
        onSetLocalSharingEnabled = viewModel::onSetLocalSharingEnabled,
        onClickLocalSharing = viewModel::onClickLocalSharing,
        onClickOfflineStorageOptionsDialog = viewModel::onClickOfflineStorageOptionsDialog,
    )

    if(uiState.storageOptionsDialogVisible) {
        SettingsDialog(
            onDismissRequest = viewModel::onDismissOfflineStorageOptionsDialog
        ) {
            uiState.storageOptions.forEach { option ->
                ListItem(
                    modifier = Modifier.clickable {
                        viewModel.onSelectOfflineStorageOption(option.option)
                    },
                    headlineContent = { Text(stringResource(option.option.label)) },
                    supportingContent = {
                        Text(
                            stringResource(
                                MR.strings.space_available,
                                UMFileUtil.formatFileSize(option.availableSpace)
                            )
                        )
                    }
                )
            }
        }
    }

}

@Composable
fun StorageAndDataSettingsScreen(
    uiState: StorageAndSettingsUiState,
    onClickOfflineStorageOptionsDialog: () -> Unit = { },
    onSetLocalSharingEnabled: (Boolean) -> Unit = { },
    onClickLocalSharing: () -> Unit = { },
) {
    UstadVerticalScrollColumn(
        modifier = Modifier.fillMaxSize()
    )  {
        if(uiState.offlineStorageOptionVisible) {
            UstadDetailField2(
                modifier = Modifier.clickable {
                    onClickOfflineStorageOptionsDialog()
                },
                labelText = stringResource(MR.strings.offline_items_storage),
                valueText = uiState.selectedOfflineStorageOption?.label?.let {
                    stringResource(it)
                } ?: "",
                icon = Icons.Default.SdStorage,
            )
        }

        ListItem(
            modifier = Modifier.clickable {
                onClickLocalSharing()
            },
            leadingContent = {
                Icon(
                    Icons.Default.ConnectWithoutContact,
                    contentDescription = null,
                )
            },
            headlineContent = {
                Text(stringResource(MR.strings.local_sharing))
            },
            supportingContent = {
                Text(stringResource(MR.strings.local_sharing_subtitle))
            },
            trailingContent = {
                Switch(
                    checked = uiState.nearbySharingEnabled,
                    onCheckedChange = onSetLocalSharingEnabled,
                )
            }
        )
    }
}
