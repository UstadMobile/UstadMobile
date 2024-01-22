package com.ustadmobile.libuicompose.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.SnackBarDispatcher
import com.ustadmobile.core.impl.nav.NavResultReturner
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.libuicompose.effects.AppUiStateEffect
import com.ustadmobile.libuicompose.effects.NavCommandEffect
import com.ustadmobile.libuicompose.nav.UstadNavControllerPreCompose
import com.ustadmobile.libuicompose.nav.UstadSavedStateHandlePreCompose
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.map
import moe.tlaster.precompose.navigation.BackStackEntry
import moe.tlaster.precompose.viewmodel.viewModel
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.compose.localDI
import org.kodein.di.singleton
import kotlin.reflect.KClass

@Composable
fun <T: UstadViewModel> ustadViewModel(
    modelClass: KClass<T>,
    backStackEntry: BackStackEntry,
    navController: UstadNavControllerPreCompose,
    onSetAppUiState: (AppUiState) -> Unit,
    navResultReturner: NavResultReturner,
    onShowSnackBar: SnackBarDispatcher,
    name: String? = null,
    appUiStateMap: ((AppUiState) -> AppUiState)? = null,
    savedStateHandle: UstadSavedStateHandlePreCompose = UstadSavedStateHandlePreCompose(backStackEntry),
    block: (di: DI, savedStateHandle: UstadSavedStateHandle) -> T,
) : T {
    val di = localDI()
    val diWithResultReturner = remember {
        DI {
            extend(di)
            bind<NavResultReturner>() with singleton { navResultReturner }
            bind<SnackBarDispatcher>() with singleton { onShowSnackBar }
        }
    }

    val viewModelNameHash = remember {
        modelClass.qualifiedName?.hashCode() ?: 0
    }

    val viewModel =  viewModel(
        modelClass = modelClass,
        keys = listOf(viewModelNameHash, savedStateHandle.argsHash),
    ) {
        Napier.d("ustadViewModel(${name ?: ""}): create viewmodel for: ${modelClass.qualifiedName}")
        block(diWithResultReturner, savedStateHandle)
    }

    NavCommandEffect(
        navController = navController,
        navCommandFlow = viewModel.navCommandFlow,
    )

    val uiStateFlow = remember(viewModel.appUiState, appUiStateMap) {
        if(appUiStateMap != null) {
            viewModel.appUiState.map(appUiStateMap)
        }else {
            viewModel.appUiState
        }
    }

    AppUiStateEffect(
        appUiStateFlow = uiStateFlow,
        onSetAppUiState = onSetAppUiState,
    )

    return viewModel
}
