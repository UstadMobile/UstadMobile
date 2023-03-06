package com.ustadmobile.view

import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import mui.icons.material.Done
import mui.icons.material.DoneAll
import react.create

class ClazzAssignmentDetailOverviewScreen {
}

val ASSIGNMENT_STATUS_MAP = mapOf(
    CourseAssignmentSubmission.NOT_SUBMITTED to Done.create(),
    CourseAssignmentSubmission.SUBMITTED to Done.create(),
    CourseAssignmentSubmission.MARKED to DoneAll.create()
)