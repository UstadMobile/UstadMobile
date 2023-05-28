package com.ustadmobile.port.android.view.clazzassignment

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Pending
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission

object ClazzAssignmentConstants {

    val SUBMISSION_STATUS_ICON_MAP = mapOf(
        CourseAssignmentSubmission.MARKED to Icons.Filled.DoneAll,
        CourseAssignmentSubmission.SUBMITTED to Icons.Filled.Done,
        CourseAssignmentSubmission.NOT_SUBMITTED to Icons.Filled.Pending,
    )

}
