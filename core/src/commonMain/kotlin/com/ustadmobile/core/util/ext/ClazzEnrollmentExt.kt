package com.ustadmobile.core.util.ext

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.ClazzEnrollment
import com.ustadmobile.lib.db.entities.ClazzEnrollment.Companion.ROLE_STUDENT_PENDING

val ROLE_TO_MESSAGEID_MAP = mapOf(
        ClazzEnrollment.ROLE_STUDENT to MessageID.student,
        ClazzEnrollment.ROLE_STUDENT_PENDING to MessageID.student,
        ClazzEnrollment.ROLE_TEACHER to MessageID.teacher
)

fun ClazzEnrollment.roleToString(context: Any, systemImpl: UstadMobileSystemImpl): String {
    val roleMessageId = ROLE_TO_MESSAGEID_MAP[clazzEnrollmentRole] ?: 0

    var roleStr = systemImpl.getString(roleMessageId, context)
    if(clazzEnrollmentRole == ClazzEnrollment.ROLE_STUDENT_PENDING) {
        roleStr += "(${systemImpl.getString(MessageID.pending, context)})"
    }

    return roleStr
}

fun ClazzEnrollment.isRolePending() = (clazzEnrollmentRole == ROLE_STUDENT_PENDING)