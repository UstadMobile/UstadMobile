package com.ustadmobile.libuicompose.view.contententry.edit

import androidx.compose.runtime.Composable
import androidx.compose.desktop.ui.tooling.preview.Preview
import com.ustadmobile.core.impl.ContainerStorageDir
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditUiState
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditUiState
import com.ustadmobile.lib.db.entities.ContentEntryWithBlockAndLanguage
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.CourseBlock

@Composable
@Preview
fun ContentEntryEditScreenPreview() {
    ContentEntryEditScreen(
        uiState = ContentEntryEditUiState(
            entity = ContentEntryWithBlockAndLanguage().apply {
                leaf = true
            },
            updateContentVisible = true,
            metadataResult = MetadataResult(
                entry = ContentEntryWithLanguage(),
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
            ),
            importerId = 1
        )
    )

}