package com.ustadmobile.libuicompose.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.libuicompose.effects.AppUiStateEffect
import com.ustadmobile.libuicompose.effects.NavCommandEffect
import com.ustadmobile.libuicompose.nav.UstadSavedStateHandlePreCompose
import moe.tlaster.precompose.navigation.BackStackEntry
import moe.tlaster.precompose.viewmodel.viewModel
import org.kodein.di.DI
import org.kodein.di.compose.localDI
import kotlin.reflect.KClass

@Composable
fun <T: UstadViewModel> ustadViewModel(
    modelClass: KClass<T>,
    backStackEntry: BackStackEntry,
    navController: UstadNavController,
    onSetAppUiState: (AppUiState) -> Unit,
    block: (di: DI, savedStateHandle: UstadSavedStateHandle) -> T,
) : T {
    val di = localDI()

    //Use the query parameters as a key for the viewmodel function invalidation
    val queryParamsHash = remember(backStackEntry.queryString) {
        backStackEntry.queryString?.map?.hashCode() ?: 0
    }

    val viewModel =  viewModel(
        modelClass = modelClass,
        keys = listOf(queryParamsHash),
    ) {
        block(di, UstadSavedStateHandlePreCompose(backStackEntry))
    }

    NavCommandEffect(
        navController = navController,
        navCommandFlow = viewModel.navCommandFlow,
        savedStateHandle = UstadSavedStateHandlePreCompose(backStackEntry),
    )

    AppUiStateEffect(
        appUiStateFlow = viewModel.appUiState,
        onSetAppUiState = onSetAppUiState,
    )

    return viewModel
}
