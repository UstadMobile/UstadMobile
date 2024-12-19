package com.ustadmobile.libuicompose.view.settings.localsharing

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.viewmodel.settings.localsharing.LocalSharingSettingsUiState
import com.ustadmobile.core.viewmodel.settings.localsharing.LocalSharingSettingsViewModel
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun LocalSharingSettingsScreen(
    viewModel: LocalSharingSettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(LocalSharingSettingsUiState())

    LocalSharingSettingsScreen(uiState)
}

@Composable
fun LocalSharingSettingsScreen(
    uiState: LocalSharingSettingsUiState
) {
    UstadLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(uiState.neighbors) {
            ListItem(
                headlineContent = { Text(it.addr) }
            )
        }
    }
}
