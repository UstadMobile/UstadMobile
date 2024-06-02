package com.ustadmobile.libuicompose.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditUiState
import com.ustadmobile.lib.db.composites.CourseBlockAndEditEntities
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.CourseBlock

@Composable
@Preview
private fun CourseBlockEditPreview() {
    val uiState = CourseBlockEditUiState(
        block = CourseBlockAndEditEntities(
            courseBlock = CourseBlock().apply {
                cbMaxPoints = 78f
                cbCompletionCriteria = 14
                cbCompletionCriteria = ContentEntry.COMPLETION_CRITERIA_MIN_SCORE
            }
        ),
    )
    UstadCourseBlockEdit(uiState)
}