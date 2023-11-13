package com.ustadmobile.libuicompose.view.clazzassignment.edit

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.core.viewmodel.clazzassignment.edit.ClazzAssignmentEditUiState
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditUiState
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity

@Composable
@Preview
fun ClazzAssignmentEditScreenPreview() {
    val uiStateVal = ClazzAssignmentEditUiState(
        courseBlockEditUiState = CourseBlockEditUiState(
            courseBlock = CourseBlock().apply {
                cbMaxPoints = 78
                cbCompletionCriteria = 14
            },
        ),
        entity = CourseBlockWithEntity().apply {
            assignment = ClazzAssignment().apply {
                caMarkingType = ClazzAssignment.MARKED_BY_PEERS
            }
        }
    )

    ClazzAssignmentEditScreen(uiStateVal)
}