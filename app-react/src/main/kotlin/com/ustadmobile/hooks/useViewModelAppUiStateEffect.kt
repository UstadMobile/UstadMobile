package com.ustadmobile.hooks

import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.mui.components.UstadScreensContext
import kotlinx.coroutines.launch
import react.useEffect
import react.useRequiredContext

/**
 * When the created view model is an UstadViewModel and we have a function that handles emitted
 * AppUiState changes, use an effect to launch collection of the flow and call the function
 * to handle AppUiState change.
 */
fun useViewModelAppUiStateEffect(
    viewModel: UstadViewModel,
    collectAppUiState: Boolean = true,
) {
    val ustadScreensContext = useRequiredContext(UstadScreensContext)

    useEffect(dependencies = arrayOf(viewModel)) {
        viewModel.viewModelScope.takeIf { collectAppUiState }?.launch {
            viewModel.appUiState.collect {
                ustadScreensContext.onAppUiStateChanged(it)
            }
        }
    }
}
