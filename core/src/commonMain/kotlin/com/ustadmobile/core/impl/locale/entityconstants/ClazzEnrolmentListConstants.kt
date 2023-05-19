package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.lib.db.entities.ClazzEnrolment

object ClazzEnrolmentListConstants {
    val ROLE_TO_MESSAGE_ID_MAP: Map<Int, Int> = mapOf(
        ClazzEnrolment.ROLE_STUDENT to MessageID.student,
        ClazzEnrolment.ROLE_TEACHER to MessageID.teacher,
        ClazzEnrolment.ROLE_PARENT to MessageID.parent,
        ClazzEnrolment.ROLE_STUDENT_PENDING to MessageID.pending
    )

    val OUTCOME_TO_MESSAGE_ID_MAP: Map<Int, Int> = mapOf(
        ClazzEnrolment.OUTCOME_FAILED to MessageID.outcome,
        ClazzEnrolment.OUTCOME_GRADUATED to MessageID.graduated,
        ClazzEnrolment.OUTCOME_DROPPED_OUT to MessageID.dropped_out,
        ClazzEnrolment.OUTCOME_IN_PROGRESS to MessageID.in_progress
    )
}