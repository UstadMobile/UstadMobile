package com.ustadmobile.libuicompose.view.clazz.detail

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.nav.NavResultReturner
import com.ustadmobile.core.viewmodel.clazz.detail.ClazzDetailUiState
import com.ustadmobile.core.viewmodel.clazz.detail.ClazzDetailViewModel
import com.ustadmobile.core.viewmodel.clazz.detailoverview.ClazzDetailOverviewViewModel
import com.ustadmobile.core.viewmodel.clazzenrolment.clazzmemberlist.ClazzMemberListViewModel
import com.ustadmobile.libuicompose.components.UstadScreenTabs
import com.ustadmobile.libuicompose.nav.UstadNavControllerPreCompose
import com.ustadmobile.libuicompose.view.clazz.detailoverview.ClazzDetailOverviewScreen
import com.ustadmobile.libuicompose.view.clazzenrolment.clazzmemberlist.ClazzMemberListScreen
import com.ustadmobile.libuicompose.viewmodel.ustadViewModel
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.navigation.BackStackEntry

@Composable
fun ClazzDetailScreen(
    backStackEntry: BackStackEntry,
    navController: UstadNavControllerPreCompose,
    onSetAppUiState: (AppUiState) -> Unit,
    navResultReturner: NavResultReturner,
) {
    val clazzDetailViewModel = ustadViewModel(
        ClazzDetailViewModel::class, backStackEntry, navController, onSetAppUiState, navResultReturner
    ) { di, savedStateHandle ->
        ClazzDetailViewModel(di, savedStateHandle)
    }

    ClazzDetailScreen(clazzDetailViewModel, backStackEntry, navController, onSetAppUiState, navResultReturner)
}

@Composable
fun ClazzDetailScreen(
    viewModel: ClazzDetailViewModel,
    backStackEntry: BackStackEntry,
    navController: UstadNavControllerPreCompose,
    onSetAppUiState: (AppUiState) -> Unit,
    navResultReturner: NavResultReturner,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(
        ClazzDetailUiState(), Dispatchers.Main.immediate
    )

    ClazzDetailScreen(uiState, backStackEntry, navController, onSetAppUiState, navResultReturner)
}

@Composable
fun ClazzDetailScreen(
    uiState: ClazzDetailUiState,
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
            ClazzDetailOverviewViewModel.DEST_NAME -> {
                ClazzDetailOverviewScreen(
                    tabViewModel(ClazzDetailOverviewViewModel::class) { di, savedStateHandle ->
                        ClazzDetailOverviewViewModel(di, savedStateHandle)
                    }
                )
            }
            ClazzMemberListViewModel.DEST_NAME -> {
                ClazzMemberListScreen(
                    tabViewModel(ClazzMemberListViewModel::class) { di, savedStateHandle ->
                        ClazzMemberListViewModel(di, savedStateHandle)
                    }
                )
            }
            else -> {
                Text(it.viewName)
            }
        }
    }

}