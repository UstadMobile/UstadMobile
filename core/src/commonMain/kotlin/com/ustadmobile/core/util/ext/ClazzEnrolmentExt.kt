package com.ustadmobile.core.util.ext

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolment.Companion.ROLE_STUDENT_PENDING
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason

val ROLE_TO_MESSAGEID_MAP = mapOf(
        ClazzEnrolment.ROLE_STUDENT to MessageID.student,
        ClazzEnrolment.ROLE_STUDENT_PENDING to MessageID.student,
        ClazzEnrolment.ROLE_TEACHER to MessageID.teacher
)

fun ClazzEnrolment.roleToString(context: Any, systemImpl: UstadMobileSystemImpl): String {
    val roleMessageId = ROLE_TO_MESSAGEID_MAP[clazzEnrolmentRole] ?: 0

    var roleStr = systemImpl.getString(roleMessageId, context)
    if(clazzEnrolmentRole == ClazzEnrolment.ROLE_STUDENT_PENDING) {
        roleStr += "(${systemImpl.getString(MessageID.pending, context)})"
    }

    return roleStr
}

val STATUS_TO_MESSAGE_ID_MAP = mapOf(
        ClazzEnrolment.STATUS_ENROLED to MessageID.enroled,
        ClazzEnrolment.STATUS_DROPPED_OUT to MessageID.dropped_out,
        ClazzEnrolment.STATUS_FAILED to MessageID.failed,
        ClazzEnrolment.STATUS_GRADUATED to MessageID.graduated,
        ClazzEnrolment.STATUS_MOVED to MessageID.moved
)

fun ClazzEnrolmentWithLeavingReason.statusToString(context: Any, systemImpl: UstadMobileSystemImpl): String {
    val statusMessageId = STATUS_TO_MESSAGE_ID_MAP[clazzEnrolmentStatus] ?: 0

    var statusStr = systemImpl.getString(statusMessageId, context)
    if(clazzEnrolmentLeavingReasonUid != 0L){
        statusStr = "(${leavingReason?.leavingReasonTitle})"
    }

    return statusStr
}


fun ClazzEnrolment.isRolePending() = (clazzEnrolmentRole == ROLE_STUDENT_PENDING)