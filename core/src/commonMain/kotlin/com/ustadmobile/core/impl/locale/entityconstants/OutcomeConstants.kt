package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.MR
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.ClazzEnrolment

object OutcomeConstants {

    val OUTCOME_MESSAGE_IDS = listOf(
        MessageIdOption2(MR.strings.in_progress, ClazzEnrolment.OUTCOME_IN_PROGRESS),
        MessageIdOption2(MR.strings.graduated, ClazzEnrolment.OUTCOME_GRADUATED),
        MessageIdOption2(MR.strings.failed, ClazzEnrolment.OUTCOME_FAILED),
        MessageIdOption2(MR.strings.dropped_out, ClazzEnrolment.OUTCOME_DROPPED_OUT),
    )
}