package com.ustadmobile.libuicompose.view.contententry.getmetadata

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ustadmobile.core.viewmodel.contententry.getmetadata.ContentEntryGetMetadataUiState
import com.ustadmobile.core.viewmodel.contententry.getmetadata.ContentEntryGetMetadataViewModel
import com.ustadmobile.libuicompose.components.ProgressOrErrorMessage

@Composable
fun ContentEntryGetMetadataScreen(
    uiState: ContentEntryGetMetadataUiState
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ProgressOrErrorMessage(uiState.status.error)
    }

}

@Composable
fun ContentEntryGetMetadataScreen(
    viewModel: ContentEntryGetMetadataViewModel
)  {
    val uiState by viewModel.uiState.collectAsState(
        ContentEntryGetMetadataUiState()
    )

    ContentEntryGetMetadataScreen(uiState)
}
