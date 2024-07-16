package com.ustadmobile.libuicompose.view.clazz.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.SnackBarDispatcher
import com.ustadmobile.core.impl.nav.NavResultReturner
import com.ustadmobile.core.viewmodel.clazz.detail.ClazzDetailUiState
import com.ustadmobile.core.viewmodel.clazz.detail.ClazzDetailViewModel
import com.ustadmobile.core.viewmodel.clazz.detailoverview.ClazzDetailOverviewViewModel
import com.ustadmobile.core.viewmodel.clazz.gradebook.ClazzGradebookViewModel
import com.ustadmobile.core.viewmodel.clazzenrolment.clazzmemberlist.ClazzMemberListViewModel
import com.ustadmobile.core.viewmodel.clazzlog.attendancelist.ClazzLogListAttendanceViewModel
import com.ustadmobile.core.viewmodel.coursegroupset.list.CourseGroupSetListViewModel
import com.ustadmobile.libuicompose.components.UstadScreenTabs
import com.ustadmobile.libuicompose.components.isDesktop
import com.ustadmobile.libuicompose.nav.UstadNavControllerPreCompose
import com.ustadmobile.libuicompose.view.clazz.detailoverview.ClazzDetailOverviewScreen
import com.ustadmobile.libuicompose.view.clazz.gradebook.ClazzGradebookScreen
import com.ustadmobile.libuicompose.view.clazzenrolment.clazzmemberlist.ClazzMemberListScreen
import com.ustadmobile.libuicompose.view.clazzlog.attendancelist.ClazzLogListAttendanceScreen
import com.ustadmobile.libuicompose.view.coursegroupset.list.CourseGroupSetListScreen
import com.ustadmobile.libuicompose.viewmodel.ustadViewModel
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.navigation.BackStackEntry

@Composable
fun ClazzDetailScreen(
    backStackEntry: BackStackEntry,
    navController: UstadNavControllerPreCompose,
    onSetAppUiState: (AppUiState) -> Unit,
    onShowSnackbar: SnackBarDispatcher,
    navResultReturner: NavResultReturner,
) {
    val clazzDetailViewModel = ustadViewModel(
        ClazzDetailViewModel::class, backStackEntry, navController, onSetAppUiState,
        navResultReturner, onShowSnackbar
    ) { di, savedStateHandle ->
        ClazzDetailViewModel(di, savedStateHandle)
    }

    ClazzDetailScreen(clazzDetailViewModel, backStackEntry, navController, onSetAppUiState,
        onShowSnackbar, navResultReturner)
}

@Composable
fun ClazzDetailScreen(
    viewModel: ClazzDetailViewModel,
    backStackEntry: BackStackEntry,
    navController: UstadNavControllerPreCompose,
    onSetAppUiState: (AppUiState) -> Unit,
    onShowSnackbar: SnackBarDispatcher,
    navResultReturner: NavResultReturner,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(
        ClazzDetailUiState(), Dispatchers.Main.immediate
    )

    ClazzDetailScreen(uiState, backStackEntry, navController, onSetAppUiState, onShowSnackbar,
        navResultReturner)
}

@Composable
fun ClazzDetailScreen(
    uiState: ClazzDetailUiState,
    backStackEntry: BackStackEntry,
    navController: UstadNavControllerPreCompose,
    onSetAppUiState: (AppUiState) -> Unit,
    onShowSnackbar: SnackBarDispatcher,
    navResultReturner: NavResultReturner,
) {

    UstadScreenTabs(
        tabs = uiState.tabs,
        backStackEntry = backStackEntry,
        navController = navController,
        onSetAppUiState = onSetAppUiState,
        navResultReturner = navResultReturner,
        onShowSnackBar = onShowSnackbar,
        scrollable = !isDesktop(),
    ) { tabItem ->
        when(tabItem.viewName) {
            ClazzDetailOverviewViewModel.DEST_NAME -> {
                ClazzDetailOverviewScreen(
                    tabViewModel(ClazzDetailOverviewViewModel::class, tabItem) { di, savedStateHandle ->
                        ClazzDetailOverviewViewModel(di, savedStateHandle)
                    }
                )
            }
            ClazzMemberListViewModel.DEST_NAME -> {
                ClazzMemberListScreen(
                    tabViewModel(ClazzMemberListViewModel::class, tabItem) { di, savedStateHandle ->
                        ClazzMemberListViewModel(di, savedStateHandle)
                    }
                )
            }

            CourseGroupSetListViewModel.DEST_NAME -> {
                CourseGroupSetListScreen(
                    tabViewModel(CourseGroupSetListViewModel::class, tabItem) { di, savedStateHandle ->
                        CourseGroupSetListViewModel(di, savedStateHandle)
                    }
                )
            }

            ClazzLogListAttendanceViewModel.DEST_NAME -> {
                ClazzLogListAttendanceScreen(
                    tabViewModel(ClazzLogListAttendanceViewModel::class, tabItem) { di, savedStateHandle ->
                        ClazzLogListAttendanceViewModel(di, savedStateHandle)
                    }
                )
            }

            ClazzGradebookViewModel.DEST_NAME -> {
                ClazzGradebookScreen(
                    tabViewModel(ClazzGradebookViewModel::class, tabItem) { di, savedStateHandle ->
                        ClazzGradebookViewModel(di, savedStateHandle)
                    }
                )
            }
        }
    }

}