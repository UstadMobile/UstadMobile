package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.MR
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import dev.icerock.moko.resources.StringResource

object ClazzEnrolmentListConstants {
    val ROLE_TO_STRING_RESOURCE_MAP: Map<Int, StringResource> = mapOf(
        ClazzEnrolment.ROLE_STUDENT to MR.strings.student,
        ClazzEnrolment.ROLE_TEACHER to MR.strings.teacher,
        ClazzEnrolment.ROLE_PARENT to MR.strings.parent,
        ClazzEnrolment.ROLE_STUDENT_PENDING to MR.strings.pending
    )

    val OUTCOME_TO_STRING_RESOURCE_MAP: Map<Int, StringResource> = mapOf(
        ClazzEnrolment.OUTCOME_FAILED to MR.strings.outcome,
        ClazzEnrolment.OUTCOME_GRADUATED to MR.strings.graduated,
        ClazzEnrolment.OUTCOME_DROPPED_OUT to MR.strings.dropped_out,
        ClazzEnrolment.OUTCOME_IN_PROGRESS to MR.strings.in_progress
    )
}