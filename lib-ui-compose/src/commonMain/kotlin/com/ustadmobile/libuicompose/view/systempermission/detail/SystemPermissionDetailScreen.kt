package com.ustadmobile.libuicompose.view.systempermission.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.viewmodel.systempermission.detail.SystemPermissionDetailUiState
import com.ustadmobile.core.viewmodel.systempermission.detail.SystemPermissionDetailViewModel
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadPermissionLabelsListItems

@Composable
fun SystemPermissionDetailScreen(
    viewModel: SystemPermissionDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState(SystemPermissionDetailUiState())

    SystemPermissionDetailScreen(
        uiState = uiState
    )
}
@Composable
fun SystemPermissionDetailScreen(
    uiState: SystemPermissionDetailUiState
) {
    UstadLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        UstadPermissionLabelsListItems(
            permissionLabels = uiState.permissionLabels,
            value = uiState.systemPermission?.spPermissionsFlag ?: 0
        )
    }
}
