package com.ustadmobile.core.util.ext

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolment.Companion.ROLE_STUDENT_PENDING

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

fun ClazzEnrolment.isRolePending() = (clazzEnrolmentRole == ROLE_STUDENT_PENDING)