package com.ustadmobile.libuicompose.view.clazzassignment.detailoverview

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.TaskAlt
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission

object ClazzAssignmentDetailOverviewConstants {

    @JvmField
    val ASSIGNMENT_STATUS_MAP = mapOf(
        CourseAssignmentSubmission.NOT_SUBMITTED to Icons.Default.Done,
        CourseAssignmentSubmission.SUBMITTED to Icons.Default.Done,
        CourseAssignmentSubmission.MARKED to Icons.Default.DoneAll
    )

    @JvmField
    val SUBMISSION_POLICY_MAP = mapOf(
        ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE to Icons.Default.TaskAlt,
        ClazzAssignment.SUBMISSION_POLICY_MULTIPLE_ALLOWED to Icons.Default.AddTask,
    )


}