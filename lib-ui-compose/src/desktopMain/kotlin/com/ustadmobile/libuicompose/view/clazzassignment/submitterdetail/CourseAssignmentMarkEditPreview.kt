package com.ustadmobile.libuicompose.view.clazzassignment.submitterdetail

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.MR
import com.ustadmobile.lib.db.entities.CourseAssignmentMark


@Composable
@Preview
private fun CourseAssignmentMarkEditPreview() {
    CourseAssignmentMarkEdit(
        draftMark = CourseAssignmentMark().apply {
            camMarkerComment = "Awful"
            camMark = 1f
        },
        maxPoints = 10f,
        scoreError = null,
        onChangeDraftMark = { },
        onClickSubmitGrade = { },
        submitGradeButtonMessageId = MR.strings.submit,
        submitGradeButtonAndGoNextMessageId = MR.strings.submit_grade_and_mark_next,
        onClickSubmitGradeAndMarkNext = { }
    )
}
