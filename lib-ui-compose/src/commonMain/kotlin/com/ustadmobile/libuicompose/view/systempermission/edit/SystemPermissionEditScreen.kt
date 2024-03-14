package com.ustadmobile.libuicompose.view.systempermission.edit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.viewmodel.systempermission.edit.SystemPermissionEditUiState
import com.ustadmobile.core.viewmodel.systempermission.edit.SystemPermissionEditViewModel
import com.ustadmobile.libuicompose.components.UstadPermissionEdit

@Composable
fun SystemPermissionEditScreen(viewModel: SystemPermissionEditViewModel){
    val uiState by viewModel.uiState.collectAsState(SystemPermissionEditUiState())

    SystemPermissionEditScreen(
        uiState = uiState,
        onTogglePermission = viewModel::onTogglePermission
    )
}

@Composable
fun SystemPermissionEditScreen(
    uiState: SystemPermissionEditUiState,
    onTogglePermission: (Long) -> Unit,
) {
    UstadPermissionEdit(
        value = uiState.entity?.spPermissionsFlag ?: 0,
        permissionLabels = uiState.permissionLabels,
        onToggle = onTogglePermission,
        enabled = uiState.fieldsEnabled,
        modifier = Modifier.fillMaxSize(),
    )
}