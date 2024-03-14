package com.ustadmobile.view.systempermission.edit

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.viewmodel.systempermission.edit.SystemPermissionEditUiState
import com.ustadmobile.core.viewmodel.systempermission.edit.SystemPermissionEditViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadPermissionEditComponent
import com.ustadmobile.mui.components.UstadStandardContainer
import react.FC
import react.Props

val SystemPermissionEditScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        SystemPermissionEditViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(SystemPermissionEditUiState())

    SystemPermissionEditComponent {
        uiState = uiStateVal
        onTogglePermission = viewModel::onTogglePermission
    }

}

external interface SystemPermissionEditProps : Props {

    var uiState: SystemPermissionEditUiState

    var onTogglePermission: (Long) -> Unit

}

val SystemPermissionEditComponent = FC<SystemPermissionEditProps> { props ->
    UstadStandardContainer {
        UstadPermissionEditComponent {
            value = props.uiState.entity?.spPermissionsFlag ?: 0
            permissionLabels = props.uiState.permissionLabels
            enabled = props.uiState.fieldsEnabled
            onToggle = props.onTogglePermission
        }
    }


}

