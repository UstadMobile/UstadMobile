package com.ustadmobile.libuicompose.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.ustadmobile.core.impl.appstate.AppUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@Composable
fun AppUiStateEffect(
    appUiStateFlow: Flow<AppUiState>,
    onSetAppUiState: (AppUiState) -> Unit,
) {
    LaunchedEffect(appUiStateFlow) {
        //Needs to use .immediate to ensure synchronous update of text state on Search
        withContext(Dispatchers.Main.immediate) {
            appUiStateFlow.collect {
                onSetAppUiState(it)
            }
        }
    }
}
