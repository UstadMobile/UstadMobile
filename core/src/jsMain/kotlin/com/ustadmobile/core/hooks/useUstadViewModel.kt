package com.ustadmobile.core.hooks

import com.ustadmobile.core.components.DIContext
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.appstate.SnackBarDispatcher
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadViewModel
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import react.useContext
import react.useMemo

/**
 * Composition to handle the UstadViewModel - includes handling of appuistate and navigation flow
 */
fun <T: UstadViewModel> useUstadViewModel(
    onAppUiStateChange: ((AppUiState) -> Unit),
    onShowSnack: ((Snack) -> Unit)? = null,
    block: (di: DI, savedStateHandle: UstadSavedStateHandle) -> T
): T {
    val contextDi = useContext(DIContext) ?: throw IllegalStateException("No DIContext!")

    val di = useMemo(dependencies = emptyArray()) {
        DI {
            extend(contextDi)
            if(onShowSnack != null){
                bind<SnackBarDispatcher>() with singleton {
                    SnackBarDispatcher {
                        onShowSnack(it)
                    }
                }
            }
        }
    }

    val viewModel = useViewModel { savedStateHandle ->
        block(di, savedStateHandle)
    }

    useViewModelAppUiStateEffect(viewModel, onAppUiStateChange)
    useNavControllerEffect(viewModel.navCommandFlow)

    return viewModel
}
