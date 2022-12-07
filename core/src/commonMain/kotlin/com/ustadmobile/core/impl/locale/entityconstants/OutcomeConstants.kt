package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.ClazzEnrolment

object OutcomeConstants {

    val OUTCOME_MESSAGE_IDS = listOf(
        MessageIdOption2(MessageID.in_progress, ClazzEnrolment.OUTCOME_IN_PROGRESS),
        MessageIdOption2(MessageID.graduated, ClazzEnrolment.OUTCOME_GRADUATED),
        MessageIdOption2(MessageID.failed, ClazzEnrolment.OUTCOME_FAILED),
        MessageIdOption2(MessageID.dropped_out, ClazzEnrolment.OUTCOME_DROPPED_OUT),
    )
}