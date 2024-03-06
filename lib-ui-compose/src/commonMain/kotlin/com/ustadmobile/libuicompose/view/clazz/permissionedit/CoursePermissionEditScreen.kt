package com.ustadmobile.libuicompose.view.clazz.permissionedit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ustadmobile.core.viewmodel.clazz.permissionedit.CoursePermissionEditUiState
import com.ustadmobile.core.viewmodel.clazz.permissionedit.CoursePermissionEditViewModel
import com.ustadmobile.libuicompose.components.UstadPermissionEdit

@Composable
fun CoursePermissionEditScreen(
    viewModel: CoursePermissionEditViewModel
) {
    val uiState by viewModel.uiState.collectAsState(CoursePermissionEditUiState())

    CoursePermissionEditScreen(
        uiState = uiState,
        onTogglePermission = viewModel::onTogglePermission
    )
}

@Composable
fun CoursePermissionEditScreen(
    uiState: CoursePermissionEditUiState,
    onTogglePermission: (Long) -> Unit,
) {
    UstadPermissionEdit(
        value = uiState.entity?.cpPermissionsFlag ?: 0,
        permissionLabels = uiState.permissionLabels,
        onToggle = onTogglePermission,
        enabled = uiState.fieldsEnabled,
    )
}
