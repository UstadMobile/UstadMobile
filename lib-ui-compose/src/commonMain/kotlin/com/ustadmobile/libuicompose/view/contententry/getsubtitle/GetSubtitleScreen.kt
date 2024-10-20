package com.ustadmobile.libuicompose.view.contententry.getsubtitle

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ustadmobile.core.viewmodel.contententry.getsubtitle.GetSubtitleUiState
import com.ustadmobile.core.viewmodel.contententry.getsubtitle.GetSubtitleViewModel
import com.ustadmobile.libuicompose.components.ProgressOrErrorMessage
import kotlinx.coroutines.Dispatchers

@Composable
fun GetSubtitleScreen(
    viewModel: GetSubtitleViewModel
){
    val uiStateVal by viewModel.uiState.collectAsState(GetSubtitleUiState(), Dispatchers.Main.immediate)

    GetSubtitleScreen(uiState = uiStateVal)
}

@Composable
fun GetSubtitleScreen(
    uiState: GetSubtitleUiState
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ProgressOrErrorMessage(uiState.error)
    }

}


