package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.MR
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.Clazz

object EnrolmentPolicyConstants {

    val ENROLMENT_POLICY_MESSAGE_IDS = listOf(
        MessageIdOption2(MR.strings.open_enrolment, Clazz.CLAZZ_ENROLMENT_POLICY_OPEN),
        MessageIdOption2(MR.strings.managed_enrolment, Clazz.CLAZZ_ENROLMENT_POLICY_WITH_LINK),
    )
}