package com.ustadmobile.view

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditUiState
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditViewModel
import com.ustadmobile.mui.components.UstadCourseBlockEdit
import mui.system.Container
import react.FC
import react.Props
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadStandardContainer

val CourseBlockEditScreen = FC<Props> {

    val viewModel = useUstadViewModel { di, savedStateHandle ->
        CourseBlockEditViewModel(di, savedStateHandle)
    }

    val uiStateVar by viewModel.uiState.collectAsState(CourseBlockEditUiState())

    UstadStandardContainer {
        UstadCourseBlockEdit {
            uiState = uiStateVar
            onCourseBlockChange = viewModel::onEntityChanged
        }
    }


}