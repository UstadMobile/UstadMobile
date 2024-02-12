package com.ustadmobile.libuicompose.view.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.viewmodel.settings.DeveloperSettingsUiState
import com.ustadmobile.core.viewmodel.settings.DeveloperSettingsViewModel
import com.ustadmobile.libuicompose.components.UstadLazyColumn

@Composable
fun DeveloperSettingsScreen(
    viewModel: DeveloperSettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsState(DeveloperSettingsUiState())

    DeveloperSettingsScreen(
        uiState = uiState,
        onClickDeveloperInfo = viewModel::onClickDeveloperInfo,
    )
}

@Composable
fun DeveloperSettingsScreen(
    uiState: DeveloperSettingsUiState,
    onClickDeveloperInfo: (Map.Entry<String, String>) -> Unit = { },
) {
    val devInfoList = uiState.developerInfo.infoMap.entries.toList()

    UstadLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = devInfoList,
            key = { it.key }
        ) {
            ListItem(
                modifier = Modifier.clickable { onClickDeveloperInfo(it) },
                headlineContent = { Text(it.key) },
                supportingContent = { Text(it.value) }
            )
        }
    }

}