package com.ustadmobile.view.contententry.detail

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.viewmodel.contententry.detail.ContentEntryDetailUiState
import com.ustadmobile.core.viewmodel.contententry.detail.ContentEntryDetailViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadScreenTabs
import react.FC
import react.Props

external interface ContentEntryDetailProps : Props {
    var uiState: ContentEntryDetailUiState
}

val ContentEntryDetailComponent = FC<ContentEntryDetailProps> { props ->
    UstadScreenTabs {
        tabs = props.uiState.tabs
        autoHideIfOneTab = true
    }
}

val ContentEntryDetailScreen = FC<Props> {
    val viewModel = useUstadViewModel(
        collectAppUiState = false
    ) { di, savedStateHandle ->
        ContentEntryDetailViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(ContentEntryDetailUiState())

    ContentEntryDetailComponent {
        uiState = uiStateVal
    }
}
