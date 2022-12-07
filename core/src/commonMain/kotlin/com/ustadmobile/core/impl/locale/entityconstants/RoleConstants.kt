package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.ClazzEnrolment

object RoleConstants {

    val ROLE_MESSAGE_IDS = listOf(
        MessageIdOption2(MessageID.student, ClazzEnrolment.ROLE_STUDENT),
        MessageIdOption2(MessageID.teacher, ClazzEnrolment.ROLE_TEACHER),
    )
}