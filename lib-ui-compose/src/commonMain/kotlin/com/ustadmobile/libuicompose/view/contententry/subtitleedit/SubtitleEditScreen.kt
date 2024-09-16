package com.ustadmobile.libuicompose.view.contententry.subtitleedit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.viewmodel.contententry.subtitleedit.SubtitleEditUiState
import com.ustadmobile.core.viewmodel.contententry.subtitleedit.SubtitleEditViewModel
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import kotlinx.coroutines.Dispatchers

@Composable
fun SubtitleEditScreen(
    viewModel: SubtitleEditViewModel
) {
    val uiState by viewModel.uiState.collectAsState(SubtitleEditUiState(), Dispatchers.Main.immediate)

    SubtitleEditScreen(
        uiState = uiState
    )
}

@Composable
fun SubtitleEditScreen(
    uiState: SubtitleEditUiState
) {
    UstadVerticalScrollColumn(
        modifier = Modifier.fillMaxSize()
    ) {

    }
}
