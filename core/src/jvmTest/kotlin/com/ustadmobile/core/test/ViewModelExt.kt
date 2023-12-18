package com.ustadmobile.core.test

import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.viewmodel.UstadViewModel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout


suspend fun UstadViewModel.awaitAppUiStateWithActionButtonVisible(
    timeout: Long = 5_000
): AppUiState = withTimeout(timeout) {
    appUiState.filter { it.actionBarButtonState.visible }.first()
}
