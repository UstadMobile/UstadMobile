package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.lib.db.entities.ClazzMember
import kotlin.jvm.JvmStatic

object ClazzMemberConstants  {

    @JvmStatic
    val ROLE_MESSAGE_ID_OTIONS = mapOf(ClazzMember.ROLE_STUDENT to MessageID.student,
            ClazzMember.ROLE_TEACHER to MessageID.teacher)

}