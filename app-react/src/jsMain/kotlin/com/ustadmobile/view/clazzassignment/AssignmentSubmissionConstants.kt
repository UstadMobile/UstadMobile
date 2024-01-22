package com.ustadmobile.view.clazzassignment

import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import mui.icons.material.DoneAll as DoneAllIcon
import mui.icons.material.Done as DoneIcon
import mui.icons.material.PendingActions as PendingActionsIcon

val SUBMISSION_STATUS_ICON_MAP = mapOf(
    CourseAssignmentSubmission.SUBMITTED to DoneIcon,
    CourseAssignmentSubmission.NOT_SUBMITTED to PendingActionsIcon,
    CourseAssignmentSubmission.MARKED to DoneAllIcon,
)
