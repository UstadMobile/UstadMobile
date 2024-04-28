package com.ustadmobile.libuicompose.view.contententry.edit

import androidx.compose.runtime.Composable
import androidx.compose.desktop.ui.tooling.preview.Preview
import com.ustadmobile.core.impl.ContainerStorageDir
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditUiState
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.lib.db.composites.ContentEntryAndContentJob
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage

@Composable
@Preview
fun ContentEntryEditScreenPreview() {
    ContentEntryEditScreen(
        uiState = ContentEntryEditUiState(
            entity = ContentEntryAndContentJob(
                entry = ContentEntry().apply {
                    title = "Hello World"
                }
            ),
            updateContentVisible = true,
            metadataResult = MetadataResult(
                entry = ContentEntryWithLanguage(),
                importerId = 0
            ),
            selectedContainerStorageDir = ContainerStorageDir(
                name = "Device Memory",
                dirUri = ""
            )
        )
    )

}