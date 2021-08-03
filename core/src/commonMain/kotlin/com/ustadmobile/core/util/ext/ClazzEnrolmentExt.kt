package com.ustadmobile.core.util.ext

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolment.Companion.ROLE_STUDENT_PENDING
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithClazz
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason

val ROLE_TO_MESSAGEID_MAP = mapOf(
        ClazzEnrolment.ROLE_STUDENT to MessageID.student,
        ClazzEnrolment.ROLE_STUDENT_PENDING to MessageID.student,
        ClazzEnrolment.ROLE_TEACHER to MessageID.teacher,
        ClazzEnrolment.ROLE_PARENT to MessageID.parent
)

fun ClazzEnrolment.roleToString(context: Any, systemImpl: UstadMobileSystemImpl): String {
    val roleMessageId = ROLE_TO_MESSAGEID_MAP[clazzEnrolmentRole] ?: 0

    var roleStr = systemImpl.getString(roleMessageId, context)
    if(clazzEnrolmentRole == ClazzEnrolment.ROLE_STUDENT_PENDING) {
        roleStr += "(${systemImpl.getString(MessageID.pending, context)})"
    }

    return roleStr
}

val OUTCOME_TO_MESSAGE_ID_MAP = mapOf(
        ClazzEnrolment.OUTCOME_IN_PROGRESS to MessageID.in_progress,
        ClazzEnrolment.OUTCOME_DROPPED_OUT to MessageID.dropped_out,
        ClazzEnrolment.OUTCOME_FAILED to MessageID.failed,
        ClazzEnrolment.OUTCOME_GRADUATED to MessageID.graduated)

fun ClazzEnrolment.outcomeToString(context: Any, systemImpl: UstadMobileSystemImpl): String {
    val outcomeMessageId = OUTCOME_TO_MESSAGE_ID_MAP[clazzEnrolmentOutcome] ?: 0

    var outcomeStr = systemImpl.getString(outcomeMessageId, context)
    if(this is ClazzEnrolmentWithLeavingReason) {
        if (clazzEnrolmentLeavingReasonUid != 0L) {
            outcomeStr += " (${leavingReason?.leavingReasonTitle})"
        }
    }

    return outcomeStr
}

fun ClazzEnrolment.isRolePending() = (clazzEnrolmentRole == ROLE_STUDENT_PENDING)