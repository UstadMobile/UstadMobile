package com.ustadmobile.core.util.ext

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolment.Companion.ROLE_STUDENT_PENDING
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithClazz
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import dev.icerock.moko.resources.StringResource

val ROLE_TO_MESSAGEID_MAP = mapOf(
        ClazzEnrolment.ROLE_STUDENT to MR.strings.student,
        ClazzEnrolment.ROLE_STUDENT_PENDING to MR.strings.student,
        ClazzEnrolment.ROLE_TEACHER to MR.strings.teacher,
        ClazzEnrolment.ROLE_PARENT to MR.strings.parent
)

fun ClazzEnrolment.roleToString(context: Any, systemImpl: UstadMobileSystemImpl): String {
    val roleMessageId = ROLE_TO_MESSAGEID_MAP[clazzEnrolmentRole] ?: MR.strings.unset

    var roleStr = systemImpl.getString(roleMessageId)
    if(clazzEnrolmentRole == ClazzEnrolment.ROLE_STUDENT_PENDING) {
        roleStr += "(${systemImpl.getString(MR.strings.pending)})"
    }

    return roleStr
}

val OUTCOME_TO_MESSAGE_ID_MAP = mapOf(
        ClazzEnrolment.OUTCOME_IN_PROGRESS to MR.strings.in_progress,
        ClazzEnrolment.OUTCOME_DROPPED_OUT to MR.strings.dropped_out,
        ClazzEnrolment.OUTCOME_FAILED to MR.strings.failed,
        ClazzEnrolment.OUTCOME_GRADUATED to MR.strings.graduated)

fun ClazzEnrolment.outcomeToString(context: Any, systemImpl: UstadMobileSystemImpl): String {
    val outcomeMessageId = OUTCOME_TO_MESSAGE_ID_MAP[clazzEnrolmentOutcome] ?: MR.strings.unset

    var outcomeStr = systemImpl.getString(outcomeMessageId)
    if(this is ClazzEnrolmentWithLeavingReason) {
        if (clazzEnrolmentLeavingReasonUid != 0L) {
            outcomeStr += " (${leavingReason?.leavingReasonTitle})"
        }
    }

    return outcomeStr
}

fun ClazzEnrolment.isRolePending() = (clazzEnrolmentRole == ROLE_STUDENT_PENDING)