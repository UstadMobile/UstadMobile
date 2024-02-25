package com.ustadmobile.view.clazz.permissionedit

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.viewmodel.clazz.permissionedit.CoursePermissionEditUiState
import com.ustadmobile.core.viewmodel.clazz.permissionedit.CoursePermissionEditViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadPermissionEditComponent
import com.ustadmobile.mui.components.UstadStandardContainer
import react.FC
import react.Props

val CoursePermissionEditScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        CoursePermissionEditViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(CoursePermissionEditUiState())

    UstadStandardContainer {
        CoursePermissionEditComponent {
            uiState = uiStateVal
            onToggle = viewModel::onTogglePermission
        }
    }


}

external interface CoursePermissionEditProps: Props {

    var uiState: CoursePermissionEditUiState

    var onToggle: (Long) -> Unit

}

val CoursePermissionEditComponent = FC<CoursePermissionEditProps> { props ->
    UstadPermissionEditComponent {
        permissionLabels = props.uiState.permissionLabels
        value = props.uiState.entity?.cpPermissionsFlag ?: 0
        onToggle = props.onToggle
        enabled = props.uiState.fieldsEnabled
    }
}

