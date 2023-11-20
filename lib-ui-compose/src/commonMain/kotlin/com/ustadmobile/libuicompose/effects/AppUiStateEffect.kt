package com.ustadmobile.libuicompose.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.ustadmobile.core.impl.appstate.AppUiState
import kotlinx.coroutines.flow.Flow

@Composable
fun AppUiStateEffect(
    appUiStateFlow: Flow<AppUiState>,
    onSetAppUiState: (AppUiState) -> Unit,
) {
    LaunchedEffect(appUiStateFlow) {
        appUiStateFlow.collect {
            onSetAppUiState(it)
        }
    }
}
