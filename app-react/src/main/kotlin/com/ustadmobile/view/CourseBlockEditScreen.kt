package com.ustadmobile.view

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.viewmodel.CourseBlockEditUiState
import com.ustadmobile.core.viewmodel.CourseBlockEditViewModel
import com.ustadmobile.mui.components.UstadCourseBlockEdit
import mui.system.Container
import react.FC
import react.Props
import com.ustadmobile.hooks.useUstadViewModel

val CourseBlockEditScreen = FC<Props> {

    val viewModel = useUstadViewModel { di, savedStateHandle ->
        CourseBlockEditViewModel(di, savedStateHandle)
    }

    val uiStateVar by viewModel.uiState.collectAsState(CourseBlockEditUiState())

    Container {
        UstadCourseBlockEdit {
            uiState = uiStateVar
            onCourseBlockChange = viewModel::onEntityChanged
        }
    }


}