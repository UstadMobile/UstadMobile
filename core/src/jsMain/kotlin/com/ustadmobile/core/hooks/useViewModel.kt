package com.ustadmobile.core.hooks

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.navigation.SavedStateHandle2
import com.ustadmobile.core.viewmodel.ViewModel
import io.github.aakira.napier.Napier
import kotlinx.browser.window
import react.*
import react.router.dom.useSearchParams
import react.router.useLocation
import web.url.URLSearchParams

private data class ViewModelAndKey<T: ViewModel>(
    val viewModel: T,
    val locationKey: String,
)

/**
 * Use a ViewModel via useEffect and useState. The ViewModel will be cleared when the component is
 * unmounted. It will be recreated if the search param arguments change.
 *
 * @param viewModelFactory function that will create the viewmodel. SavedStateHandle will be given as argument
 */
fun <T:ViewModel> useViewModel(
    overrideSearchParams: URLSearchParams? = null,
    viewModelFactory: (savedStateHandle: UstadSavedStateHandle) -> T
): T{
    val (searchParams, _) = useSearchParams()

    val locationKey = useLocation().key

    var viewModelAndKey: ViewModelAndKey<T> by useState {
        val savedStateHandle = SavedStateHandle2(window.history,
            overrideSearchParams ?: searchParams)
        ViewModelAndKey(
            viewModelFactory(savedStateHandle), locationKey
        ).also {
            Napier.d("Creating ViewModel: ${it.viewModel::class.simpleName}")
        }
    }

    useEffect(locationKey){
        if(viewModelAndKey.locationKey != locationKey) {
            viewModelAndKey = ViewModelAndKey(
                viewModelFactory(SavedStateHandle2(window.history, searchParams)),
                locationKey
            )

            Napier.d("Recreating ViewModel ${viewModelAndKey.viewModel::class.simpleName}")
        }

        cleanup {
            Napier.d("Close ViewModel: ${viewModelAndKey.viewModel::class.simpleName}")
            viewModelAndKey.viewModel.close()
        }
    }

    return viewModelAndKey.viewModel
}

