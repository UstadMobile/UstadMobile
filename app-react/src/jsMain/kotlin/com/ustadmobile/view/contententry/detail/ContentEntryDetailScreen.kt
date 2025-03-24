package com.ustadmobile.view.contententry.detail

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.viewmodel.contententry.detail.ContentEntryDetailUiState
import com.ustadmobile.core.viewmodel.contententry.detail.ContentEntryDetailViewModel
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsPersonListViewModel
import com.ustadmobile.core.viewmodel.contententry.detailoverviewtab.ContentEntryDetailOverviewViewModel
import com.ustadmobile.entities.UstadScreen
import com.ustadmobile.entities.UstadScreens
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadScreenTabs
import com.ustadmobile.view.contententry.detailattemptstab.ContentEntryDetailAttemptsPersonListScreen
import com.ustadmobile.view.contententry.detailoverviewtab.ContentEntryDetailOverviewScreen
import react.FC
import react.Props

external interface ContentEntryDetailProps : Props {
    var uiState: ContentEntryDetailUiState
}

private val OVERVIEW_ATTEMPTS_TAB_SCREENS: UstadScreens = listOf(
    UstadScreen(
        ContentEntryDetailOverviewViewModel.DEST_NAME, "ContentEntryDetailOverview",
        ContentEntryDetailOverviewScreen
    ),
    UstadScreen(
        ContentEntryDetailAttemptsPersonListViewModel.DEST_NAME, "ContentEntryDetailAttemptsPersonList",
        ContentEntryDetailAttemptsPersonListScreen
    ),
)

val ContentEntryDetailComponent = FC<ContentEntryDetailProps> { props ->
    UstadScreenTabs {
        tabs = props.uiState.tabs
        screens= OVERVIEW_ATTEMPTS_TAB_SCREENS
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
