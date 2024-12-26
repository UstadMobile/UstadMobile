package com.ustadmobile.libuicompose.view.settings.localsharing

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
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
        onEnabledChanged = viewModel::onEnabledChanged
    )
}

@Composable
fun LocalSharingSettingsScreen(
    uiState: LocalSharingSettingsUiState,
    onEnabledChanged: (Boolean) -> Unit = { },
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
            item("neighbors_header") {
                UstadDetailHeader(
                    headerContent = { Text(stringResource(MR.strings.available_neighbors)) }
                )
            }

            items(uiState.neighbors) {
                ListItem(
                    headlineContent = { Text(it.addr) },
                    supportingContent = { Text("${it.pingTime}ms") }
                )
            }
        }


    }
}
