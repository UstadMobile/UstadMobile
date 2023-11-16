package com.ustadmobile.libuicompose.view.contententry.edit

import androidx.compose.runtime.Composable
import androidx.compose.desktop.ui.tooling.preview.Preview
import com.ustadmobile.core.impl.ContainerStorageDir
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditUiState
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditUiState
import com.ustadmobile.lib.db.composites.ContentEntryBlockLanguageAndContentJob
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.CourseBlock

@Composable
@Preview
fun ContentEntryEditScreenPreview() {
    ContentEntryEditScreen(
        uiState = ContentEntryEditUiState(
            entity = ContentEntryBlockLanguageAndContentJob(
                entry = ContentEntry().apply {
                    title = "Hello World"
                }
            ),
            updateContentVisible = true,
            metadataResult = MetadataResult(
                entry = ContentEntryWithLanguage(),
                importerId = 0
            ),
            courseBlockEditUiState = CourseBlockEditUiState(
                courseBlock = CourseBlock().apply {
                    cbMaxPoints = 78
                    cbCompletionCriteria = 14
                },
            ),
            storageOptions = listOf(
                ContainerStorageDir(
                    name = "Device Memory",
                    dirUri = ""
                ),
                ContainerStorageDir(
                    name = "Memory Card",
                    dirUri = ""
                ),
            ),
            selectedContainerStorageDir = ContainerStorageDir(
                name = "Device Memory",
                dirUri = ""
            )
        )
    )

}