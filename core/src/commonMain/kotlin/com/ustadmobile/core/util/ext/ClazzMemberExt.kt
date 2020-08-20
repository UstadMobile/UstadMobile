package com.ustadmobile.core.util.ext

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.ClazzMember
import com.ustadmobile.lib.db.entities.ClazzMember.Companion.ROLE_STUDENT_PENDING

val ROLE_TO_MESSAGEID_MAP = mapOf(
        ClazzMember.ROLE_STUDENT to MessageID.student,
        ClazzMember.ROLE_STUDENT_PENDING to MessageID.student,
        ClazzMember.ROLE_TEACHER to MessageID.teacher
)

fun ClazzMember.roleToString(context: Any, systemImpl: UstadMobileSystemImpl): String {
    val roleMessageId = ROLE_TO_MESSAGEID_MAP[clazzMemberRole] ?: 0

    var roleStr = systemImpl.getString(roleMessageId, context)
    if(clazzMemberRole == ClazzMember.ROLE_STUDENT_PENDING) {
        roleStr += "(${systemImpl.getString(MessageID.pending, context)})"
    }

    return roleStr
}

fun ClazzMember.isRolePending() = (clazzMemberRole == ROLE_STUDENT_PENDING)