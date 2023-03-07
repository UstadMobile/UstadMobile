package com.ustadmobile.core.hooks

import com.ustadmobile.core.components.DIContext
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.appstate.SnackBarDispatcher
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.navigation.SavedStateHandle2
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.ViewModel
import kotlinx.browser.window
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import react.*
import react.router.useLocation

/**
 * Use a ViewModel via useEffect and useState. The ViewModel will be cleared when the component is
 * unmounted. It will be recreated if the search param arguments change.
 */
fun <T:ViewModel> useViewModel(
    onAppUiStateChange: ((AppUiState) -> Unit)? = null,
    onShowSnack: ((Snack) -> Unit)? = null,
    block: (di: DI, savedStateHandle: UstadSavedStateHandle) -> T
): T {
    val contextDi = useContext(DIContext)

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

    var firstRun by useState { true }
    val searchStr = useLocation().search

    var viewModel: T by useState {
        console.log("Creating ViewModel")
        block(di, SavedStateHandle2(window.history))
    }

    useEffect(searchStr){
        if(!firstRun) {
            viewModel = block(di, SavedStateHandle2(window.history))
            console.log("Recreating ViewModel")
        }

        firstRun = false

        cleanup {
            console.log("Close ViewModel")
            viewModel.close()
        }
    }

    useViewModelAppUiStateEffect(viewModel, onAppUiStateChange)
    useNavControllerEffect((viewModel as? UstadViewModel)?.navCommandFlow)

    return viewModel
}

