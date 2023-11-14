package com.ustadmobile.libuicompose.view.contententry.importlink

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.contententry.importlink.ContentEntryImportLinkUiState


@Composable
@Preview
fun ContentEntryImportLinkScreenPreview(){
    ContentEntryImportLinkScreen(
        uiState = ContentEntryImportLinkUiState(
            url = "site.com/link"
        )
    )
}
