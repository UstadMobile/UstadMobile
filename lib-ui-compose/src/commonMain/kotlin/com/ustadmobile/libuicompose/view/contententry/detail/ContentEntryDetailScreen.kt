package com.ustadmobile.libuicompose.view.contententry.detail

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.SnackBarDispatcher
import com.ustadmobile.core.impl.nav.NavResultReturner
import com.ustadmobile.core.viewmodel.contententry.detail.ContentEntryDetailViewModel
import com.ustadmobile.core.viewmodel.contententry.detailoverviewtab.ContentEntryDetailOverviewViewModel
import com.ustadmobile.libuicompose.components.UstadScreenTabs
import com.ustadmobile.libuicompose.nav.UstadNavControllerPreCompose
import com.ustadmobile.libuicompose.view.contententry.detailoverviewtab.ContentEntryDetailOverviewScreen
import com.ustadmobile.libuicompose.viewmodel.ustadViewModel
import moe.tlaster.precompose.navigation.BackStackEntry

@Composable
fun ContentEntryDetailScreen(
    backStackEntry: BackStackEntry,
    navController: UstadNavControllerPreCompose,
    onSetAppUiState: (AppUiState) -> Unit,
    onShowSnackBar: SnackBarDispatcher,
    navResultReturner: NavResultReturner,
) {
    val viewModel = ustadViewModel(
        ContentEntryDetailViewModel::class, backStackEntry, navController, onSetAppUiState,
        navResultReturner, onShowSnackBar
    ) { di, savedStateHandle ->
        ContentEntryDetailViewModel(di, savedStateHandle)
    }

    val uiState by viewModel.uiState.collectAsState()

    UstadScreenTabs(
        tabs = uiState.tabs,
        backStackEntry = backStackEntry,
        navController = navController,
        onSetAppUiState = onSetAppUiState,
        onShowSnackBar = onShowSnackBar,
        navResultReturner = navResultReturner,
        autoHideIfOneTab = true,
    ) { currentTab ->
        when(currentTab.viewName) {
            ContentEntryDetailOverviewViewModel.DEST_NAME -> {
                ContentEntryDetailOverviewScreen(
                    tabViewModel(ContentEntryDetailOverviewViewModel::class, currentTab) { di, savedStateHandle ->
                        ContentEntryDetailOverviewViewModel(di, savedStateHandle)
                    }
                )
            }
            else -> {
                Text(currentTab.viewName)
            }
        }
    }
}