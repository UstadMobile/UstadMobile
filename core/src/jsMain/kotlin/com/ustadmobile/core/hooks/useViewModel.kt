package com.ustadmobile.core.hooks

import com.ustadmobile.core.components.DIContext
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.navigation.SavedStateHandle2
import com.ustadmobile.core.viewmodel.ViewModel
import kotlinx.browser.window
import org.kodein.di.DI
import react.*
import react.router.dom.useSearchParams

/**
 * Use a ViewModel via useEffect and useState. The ViewModel will be cleared when the component is
 * unmounted. It will be recreated if the search param arguments change.
 */
fun <T:ViewModel> useViewModel(
    onAppUiStateChange: ((AppUiState) -> Unit)? = null,
    block: (di: DI, savedStateHandle: UstadSavedStateHandle) -> T
): T {
    val di = useContext(DIContext)

    var firstRun by useState { true }

    val searchParams by useSearchParams()

    var viewModel: T by useState {
        console.log("Creating ViewModel")
        block(di, SavedStateHandle2(window.history))
    }

    useEffect(dependencies = arrayOf(searchParams)){
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

    return viewModel
}

