package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import kotlin.jvm.JvmStatic

object ClazzEnrolmentConstants  {

    @JvmStatic
    val ROLE_MESSAGE_ID_OPTIONS = mapOf(ClazzEnrolment.ROLE_STUDENT to MessageID.student,
            ClazzEnrolment.ROLE_TEACHER to MessageID.teacher)

}