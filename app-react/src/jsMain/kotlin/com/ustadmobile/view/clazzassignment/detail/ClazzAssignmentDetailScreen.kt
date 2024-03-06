package com.ustadmobile.view.clazzassignment.detail

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.viewmodel.clazzassignment.detail.ClazzAssignmentDetailUiState
import com.ustadmobile.core.viewmodel.clazzassignment.detail.ClazzAssignmentDetailViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.detail.submissionstab.ClazzAssignmentDetailSubmissionsTabViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewViewModel
import com.ustadmobile.entities.UstadScreen
import com.ustadmobile.entities.UstadScreens
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadScreenTabs
import com.ustadmobile.view.clazzassignment.detail.submissionstab.ClazzAssignmentDetailSubmissionsTabScreen
import com.ustadmobile.view.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewScreen
import react.FC
import react.Props

val ClazzAssignmentDetailScreen = FC<Props> {
    val viewModel = useUstadViewModel(collectAppUiState = false) { di, savedStateHandle ->
        ClazzAssignmentDetailViewModel(di, savedStateHandle )
    }

    val uiStateVal: ClazzAssignmentDetailUiState by viewModel.uiState
        .collectAsState(ClazzAssignmentDetailUiState())

    ClazzAssignmentDetailComponent {
        uiState = uiStateVal
    }
}

external interface ClazzAssignmentDetailProps: Props {

    var uiState: ClazzAssignmentDetailUiState

}

private val ASSIGNMENT_DETAIL_TAB_SCREENS: UstadScreens = listOf(
    UstadScreen(ClazzAssignmentDetailOverviewViewModel.DEST_NAME, "AssignmentOverview",
        ClazzAssignmentDetailOverviewScreen),
    UstadScreen(ClazzAssignmentDetailSubmissionsTabViewModel.DEST_NAME, "SubmissionsTab",
        ClazzAssignmentDetailSubmissionsTabScreen),
)

val ClazzAssignmentDetailComponent = FC<ClazzAssignmentDetailProps> { props ->
    if(props.uiState.tabs.isNotEmpty()) {
        UstadScreenTabs {
            tabs = props.uiState.tabs
            screens = ASSIGNMENT_DETAIL_TAB_SCREENS
        }
    }
}