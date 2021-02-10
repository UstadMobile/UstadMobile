package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.lib.db.entities.ClazzEnrollment
import kotlin.jvm.JvmStatic

object ClazzEnrollmentConstants  {

    @JvmStatic
    val ROLE_MESSAGE_ID_OPTIONS = mapOf(ClazzEnrollment.ROLE_STUDENT to MessageID.student,
            ClazzEnrollment.ROLE_TEACHER to MessageID.teacher)

}