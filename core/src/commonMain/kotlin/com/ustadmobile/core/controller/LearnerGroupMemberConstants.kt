package com.ustadmobile.core.controller

import com.ustadmobile.core.MR
import com.ustadmobile.lib.db.entities.LearnerGroupMember
import kotlin.jvm.JvmStatic

object LearnerGroupMemberConstants {

    @JvmStatic
    val ROLE_MESSAGE_ID_OPTIONS = mapOf(LearnerGroupMember.PARTICIPANT_ROLE to MR.strings.participant,
            LearnerGroupMember.PRIMARY_ROLE to MR.strings.primary_user)

}