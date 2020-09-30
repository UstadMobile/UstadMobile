package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.lib.db.entities.LearnerGroupMember
import kotlin.jvm.JvmStatic

object LearnerGroupMemberConstants {

    @JvmStatic
    val ROLE_MESSAGE_ID_OPTIONS = mapOf(LearnerGroupMember.PARTICIPANT_ROLE to MessageID.participant,
            LearnerGroupMember.PRIMARY_ROLE to MessageID.primary_user)

}