package com.ustadmobile.view.clazz.permissiondetail

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.viewmodel.clazz.permissiondetail.CoursePermissionDetailUiState
import com.ustadmobile.core.viewmodel.clazz.permissiondetail.CoursePermissionDetailViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadPermissionLabelsList
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.view.components.UstadFab
import react.FC
import react.Props

external interface CoursePermissionDetailProps : Props {

    var uiState: CoursePermissionDetailUiState


}

val CoursePermissionDetailComponent = FC<CoursePermissionDetailProps> { props ->
    UstadStandardContainer {
        UstadPermissionLabelsList {
            permissionLabels = props.uiState.permissionLabels
            value = props.uiState.coursePermission?.cpPermissionsFlag ?: 0
        }
    }
}

val CoursePermissionDetailScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        CoursePermissionDetailViewModel(di, savedStateHandle)
    }
    val uiStateVal by viewModel.uiState.collectAsState(
        CoursePermissionDetailUiState()
    )
    val appUiStateVal by viewModel.appUiState.collectAsState(AppUiState())

    CoursePermissionDetailComponent {
        uiState = uiStateVal
    }

    UstadFab {
        fabState = appUiStateVal.fabState
    }


}
