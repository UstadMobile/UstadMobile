package com.ustadmobile.libuicompose.view.contententry.getmetadata

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.domain.contententry.getmetadatafromuri.ContentEntryGetMetadataStatus
import com.ustadmobile.core.viewmodel.contententry.getmetadata.ContentEntryGetMetadataUiState

@Composable
@Preview
fun ContentEntryGetMetadataScreenErrorPreview(){
    ContentEntryGetMetadataScreen(
        ContentEntryGetMetadataUiState(
            status = ContentEntryGetMetadataStatus(error = "Error Message")
        )
    )
}

@Composable
@Preview
fun ContentEntryGetMetadataScreenProgressPreview(){
    ContentEntryGetMetadataScreen(
        ContentEntryGetMetadataUiState(
            status = ContentEntryGetMetadataStatus()
        )
    )
}