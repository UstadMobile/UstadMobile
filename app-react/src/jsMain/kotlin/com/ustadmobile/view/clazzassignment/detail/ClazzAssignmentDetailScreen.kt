package com.ustadmobile.view.clazzassignment.detail

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.viewmodel.clazzassignment.detail.ClazzAssignmentDetailUiState
import com.ustadmobile.core.viewmodel.clazzassignment.detail.ClazzAssignmentDetailViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadScreenTabs
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

val ClazzAssignmentDetailComponent = FC<ClazzAssignmentDetailProps> { props ->
    if(props.uiState.tabs.isNotEmpty()) {
        UstadScreenTabs {
            tabs = props.uiState.tabs
        }
    }
}