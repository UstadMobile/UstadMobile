package com.ustadmobile.view.report.detail

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailUiState
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.view.components.UstadFab
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import web.cssom.px

val ReportDetailScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ReportDetailViewModel(di, savedStateHandle)
    }

    val uiState by viewModel.uiState.collectAsState(ReportDetailUiState())

    val appState by viewModel.appUiState.collectAsState(AppUiState())

    UstadFab {
        fabState = appState.fabState
    }


    ReportDetailComponent2 {
        this.uiState = uiState
    }

}
val ReportDetailComponent2 = FC<ReportDetailProps> { props ->

    val strings = useStringProvider()

    UstadStandardContainer {
        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(8.px)
        }
    }
}

external interface ReportDetailProps : Props {
    var uiState: ReportDetailUiState
}