package com.ustadmobile.view.systempermission.detail

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.viewmodel.systempermission.detail.SystemPermissionDetailUiState
import com.ustadmobile.core.viewmodel.systempermission.detail.SystemPermissionDetailViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadPermissionLabelsList
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.view.components.UstadFab
import react.FC
import react.Props


val SystemPermissionDetailScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        SystemPermissionDetailViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(SystemPermissionDetailUiState())

    val appUiState by viewModel.appUiState.collectAsState(AppUiState())

    SystemPermissionDetailComponent {
        uiState = uiStateVal
    }

    UstadFab {
        fabState = appUiState.fabState
    }
}

external interface SystemPermissionDetailProps : Props {
    var uiState: SystemPermissionDetailUiState
}

val SystemPermissionDetailComponent = FC<SystemPermissionDetailProps> { props ->
    UstadStandardContainer {
        UstadPermissionLabelsList {
            value = props.uiState.systemPermission?.spPermissionsFlag ?: 0
            permissionLabels = props.uiState.permissionLabels
        }
    }
}