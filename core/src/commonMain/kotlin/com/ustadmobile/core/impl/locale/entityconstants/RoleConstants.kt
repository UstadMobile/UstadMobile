package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.MR
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.ClazzEnrolment

object RoleConstants {

    val ROLE_MESSAGE_IDS = listOf(
        MessageIdOption2(MR.strings.student, ClazzEnrolment.ROLE_STUDENT),
        MessageIdOption2(MR.strings.teacher, ClazzEnrolment.ROLE_TEACHER),
    )
}