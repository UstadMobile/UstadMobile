package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.lib.db.entities.ClazzMember
import com.ustadmobile.lib.db.entities.LearnerGroupMember
import kotlin.jvm.JvmStatic

object LearnerGroupMemberConstants {

    @JvmStatic
    val ROLE_MESSAGE_ID_OTIONS = mapOf(LearnerGroupMember.STUDENT_ROLE to MessageID.participant,
            LearnerGroupMember.TEACHER_ROLE to MessageID.primary_user)

}