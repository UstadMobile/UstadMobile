package com.ustadmobile.libuicompose.view.clazz.permissiondetail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.viewmodel.clazz.permissiondetail.CoursePermissionDetailUiState
import com.ustadmobile.core.viewmodel.clazz.permissiondetail.CoursePermissionDetailViewModel
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadPermissionLabelsListItems

@Composable
fun CoursePermissionDetailScreen(
    viewModel: CoursePermissionDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState(CoursePermissionDetailUiState())

    CoursePermissionDetailScreen(
        uiState = uiState,
    )
}

@Composable
fun CoursePermissionDetailScreen(
    uiState: CoursePermissionDetailUiState
) {
    UstadLazyColumn(modifier = Modifier.fillMaxSize()) {
        UstadPermissionLabelsListItems(
            permissionLabels = uiState.permissionLabels,
            value = uiState.coursePermission?.cpPermissionsFlag ?: 0
        )
    }
}
