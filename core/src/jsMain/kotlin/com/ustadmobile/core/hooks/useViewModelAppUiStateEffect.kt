package com.ustadmobile.core.hooks

import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.ViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import react.useEffect

/**
 * When the created view model is an UstadViewModel and we have a function that handles emitted
 * AppUiState changes, use an effect to launch collection of the flow and call the function
 * to handle AppUiState change.
 */
fun useViewModelAppUiStateEffect(
    viewModel: UstadViewModel,
    onAppUiStateChange: ((AppUiState) -> Unit),
) {
    useEffect(dependencies = arrayOf(viewModel)) {
        viewModel.viewModelScope.launch {
            viewModel.appUiState.collect {
                onAppUiStateChange(it)
            }
        }
    }
}
