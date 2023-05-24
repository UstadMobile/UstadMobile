package com.ustadmobile.view.clazz.detail

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.impl.appstate.TabItem
import com.ustadmobile.core.view.ClazzDetailOverviewView
import com.ustadmobile.core.viewmodel.clazz.detail.ClazzDetailUiState
import com.ustadmobile.core.viewmodel.clazz.detail.ClazzDetailViewModel
import com.ustadmobile.core.viewmodel.clazzenrolment.clazzmemberlist.ClazzMemberListViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadScreenTabs
import react.*

val ClazzDetailScreen = FC<Props> {

    val viewModel = useUstadViewModel(collectAppUiState = false) { di, savedStateHandle ->
        ClazzDetailViewModel(di, savedStateHandle)
    }

    val uiStateVal: ClazzDetailUiState by viewModel.uiState.collectAsState(ClazzDetailUiState())

    ClazzDetailComponent {
        uiState = uiStateVal
    }

}

external interface ClazzDetailProps : Props{
    var uiState: ClazzDetailUiState
}

val ClazzDetailComponent = FC<ClazzDetailProps> { props ->
    if(props.uiState.tabs.isNotEmpty()) {
        UstadScreenTabs {
            tabs = props.uiState.tabs
        }
    }
}

val ClazzDetailPreview = FC<Props> {

    ClazzDetailComponent {
        uiState = ClazzDetailUiState(
            tabs = listOf(
                TabItem(ClazzDetailOverviewView.VIEW_NAME, mapOf("clazzUid" to "1"), "Course"),
                TabItem(ClazzMemberListViewModel.DEST_NAME, mapOf("clazzUid" to "1"), "Members")
            )
        )
    }

}
