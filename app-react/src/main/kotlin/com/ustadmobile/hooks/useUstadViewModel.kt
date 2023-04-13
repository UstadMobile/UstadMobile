package com.ustadmobile.hooks

import com.ustadmobile.core.components.DIContext
import com.ustadmobile.core.hooks.useNavControllerEffect
import com.ustadmobile.core.hooks.useViewModel
import com.ustadmobile.core.impl.appstate.SnackBarDispatcher
import com.ustadmobile.core.impl.nav.NavResultReturner
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.mui.components.NavResultReturnerContext
import com.ustadmobile.mui.components.UstadScreensContext
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import react.useContext
import react.useMemo
import react.useRequiredContext

/**
 * Composition to handle the UstadViewModel - includes handling of appuistate and navigation flow
 */
fun <T: UstadViewModel> useUstadViewModel(
    block: (di: DI, savedStateHandle: UstadSavedStateHandle) -> T
): T {
    val appDi = useRequiredContext(DIContext)
    val ustadScreensContext = useRequiredContext(UstadScreensContext)
    val navResultReturner = useRequiredContext(NavResultReturnerContext)

    val di = useMemo(dependencies = emptyArray()) {
        DI {
            extend(appDi)
            bind<NavResultReturner>() with singleton {
                navResultReturner
            }
            bind<SnackBarDispatcher>() with singleton {
                SnackBarDispatcher {
                    ustadScreensContext.showSnackFunction.showSnackBar(it)
                }
            }
        }
    }

    val viewModel = useViewModel { savedStateHandle ->
        block(di, savedStateHandle)
    }

    useViewModelAppUiStateEffect(viewModel)
    useNavControllerEffect(viewModel.navCommandFlow)

    return viewModel
}
