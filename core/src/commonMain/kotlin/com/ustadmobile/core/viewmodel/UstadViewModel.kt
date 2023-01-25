package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.CommandFlowUstadNavController
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import org.kodein.di.DIAware

abstract class UstadViewModel(
    override val di: DI,
    protected val savedStateHandle: UstadSavedStateHandle,
): ViewModel(savedStateHandle), DIAware {


    protected val navController = CommandFlowUstadNavController()

    val navCommandFlow = navController.commandFlow

    protected val _appUiState = MutableStateFlow(AppUiState())

    val appUiState: Flow<AppUiState> = _appUiState.asStateFlow()

    /**
     * Shorthand to make it easier to update the loading state
     */
    protected var loadingState: LoadingUiState
        get() = _appUiState.value.loadingState
        set(value) {
            _appUiState.update {
                it.copy(loadingState = value)
            }
        }

}