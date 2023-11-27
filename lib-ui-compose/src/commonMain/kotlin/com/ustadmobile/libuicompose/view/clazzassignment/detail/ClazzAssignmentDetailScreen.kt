package com.ustadmobile.libuicompose.view.clazzassignment.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.nav.NavResultReturner
import com.ustadmobile.core.viewmodel.clazzassignment.detail.ClazzAssignmentDetailUiState
import com.ustadmobile.core.viewmodel.clazzassignment.detail.ClazzAssignmentDetailViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.detail.submissionstab.ClazzAssignmentDetailSubmissionsTabViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewViewModel
import com.ustadmobile.libuicompose.components.UstadScreenTabs
import com.ustadmobile.libuicompose.nav.UstadNavControllerPreCompose
import com.ustadmobile.libuicompose.view.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewScreen
import com.ustadmobile.libuicompose.view.clazzassignment.submissionstab.ClazzAssignmentDetailSubmissionsTabScreen
import com.ustadmobile.libuicompose.viewmodel.ustadViewModel
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.navigation.BackStackEntry

@Composable
fun ClazzAssignmentDetailScreen(
    backStackEntry: BackStackEntry,
    navController: UstadNavControllerPreCompose,
    onSetAppUiState: (AppUiState) -> Unit,
    navResultReturner: NavResultReturner,
) {
    val viewModel = ustadViewModel(
        ClazzAssignmentDetailViewModel::class, backStackEntry, navController, onSetAppUiState, navResultReturner
    ) { di, savedStateHandle ->
        ClazzAssignmentDetailViewModel(di, savedStateHandle)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle(
        ClazzAssignmentDetailUiState(), Dispatchers.Main.immediate
    )

    ClazzAssignmentDetailScreen(
        uiState = uiState,
        backStackEntry = backStackEntry,
        navController = navController,
        onSetAppUiState = onSetAppUiState,
        navResultReturner = navResultReturner
    )
}

@Composable
fun ClazzAssignmentDetailScreen(
    uiState: ClazzAssignmentDetailUiState,
    backStackEntry: BackStackEntry,
    navController: UstadNavControllerPreCompose,
    onSetAppUiState: (AppUiState) -> Unit,
    navResultReturner: NavResultReturner,
) {
    UstadScreenTabs(
        tabs = uiState.tabs,
        backStackEntry = backStackEntry,
        navController = navController,
        onSetAppUiState = onSetAppUiState,
        navResultReturner = navResultReturner
    ) {
        when(it.viewName) {
            ClazzAssignmentDetailOverviewViewModel.DEST_NAME -> {
                ClazzAssignmentDetailOverviewScreen(
                    tabViewModel(ClazzAssignmentDetailOverviewViewModel::class) { di, savedStateHandle ->
                        ClazzAssignmentDetailOverviewViewModel(di, savedStateHandle)
                    }
                )
            }

            ClazzAssignmentDetailSubmissionsTabViewModel.DEST_NAME -> {
                ClazzAssignmentDetailSubmissionsTabScreen(
                    tabViewModel(ClazzAssignmentDetailSubmissionsTabViewModel::class) { di, savedStateHandle ->
                        ClazzAssignmentDetailSubmissionsTabViewModel(di, savedStateHandle)
                    }
                )
            }
        }
    }
}