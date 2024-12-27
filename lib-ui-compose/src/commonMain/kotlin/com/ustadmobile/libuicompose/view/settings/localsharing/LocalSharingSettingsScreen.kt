package com.ustadmobile.libuicompose.view.settings.localsharing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import com.ustadmobile.core.viewmodel.settings.localsharing.LocalSharingSettingsUiState
import com.ustadmobile.core.viewmodel.settings.localsharing.LocalSharingSettingsViewModel
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.components.UstadDetailHeader
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun LocalSharingSettingsScreen(
    viewModel: LocalSharingSettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(LocalSharingSettingsUiState())

    LocalSharingSettingsScreen(
        uiState = uiState,
        onEnabledChanged = viewModel::onEnabledChanged,
        onClickDeviceName = viewModel::onClickDeviceName,
    )

    if(uiState.deviceNameDialogVisible) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDeviceNameDialog,
            confirmButton = {
                TextButton(onClick = viewModel::onClickDeviceNameDialogOk) {
                    Text(stringResource(MR.strings.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDeviceNameDialog) {
                    Text(stringResource(MR.strings.cancel))
                }
            },
            text = {
                OutlinedTextField(
                    value = uiState.deviceNameDialogText,
                    onValueChange = viewModel::onDeviceNameDialogTextChange,
                    label = { Text(stringResource(MR.strings.device_name)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            viewModel.onClickDeviceNameDialogOk()
                        }
                    )
                )
            }
        )
    }
}

@Composable
fun LocalSharingSettingsScreen(
    uiState: LocalSharingSettingsUiState,
    onEnabledChanged: (Boolean) -> Unit = { },
    onClickDeviceName: () -> Unit = { },
) {
    UstadLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item("enabled") {
            ListItem(
                modifier = Modifier.toggleable(
                    role = Role.Switch,
                    value = uiState.enabled,
                    onValueChange = onEnabledChanged,
                ),
                headlineContent = { Text(stringResource(MR.strings.enabled)) },
                trailingContent = {
                    Switch(
                        checked = uiState.enabled,
                        onCheckedChange = onEnabledChanged
                    )
                }
            )
        }

        if(uiState.enabled) {
            item("device_name") {
                ListItem(
                    modifier = Modifier.clickable {
                        onClickDeviceName()
                    },
                    leadingContent = {
                        Icon(Icons.Default.PhoneAndroid, contentDescription = null)
                    },
                    headlineContent = { Text(uiState.deviceName) },
                    supportingContent = { Text(stringResource(MR.strings.device_name)) }
                )
            }

            item("neighbors_header") {
                UstadDetailHeader(
                    headerContent = { Text(stringResource(MR.strings.available_neighbors)) }
                )
            }

            items(
                items = uiState.neighbors,
                key = { it.uid }
            ) {
                ListItem(
                    leadingContent = {
                        Icon(Icons.Default.PhoneAndroid, contentDescription = null)
                    },
                    headlineContent = { Text(it.name.ifBlank { it.addr }) },
                    supportingContent = { Text("${it.addr} - ${it.pingTime}ms") }
                )
            }
        }
    }
}
