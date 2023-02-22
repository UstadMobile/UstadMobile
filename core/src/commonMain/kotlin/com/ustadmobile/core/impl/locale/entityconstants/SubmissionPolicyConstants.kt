package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.ClazzAssignment

object SubmissionPolicyConstants {

    val SUBMISSION_POLICY_MESSAGE_IDS = listOf(

        MessageIdOption2(MessageID.multiple_submission_allowed_submission_policy,
            ClazzAssignment.SUBMISSION_POLICY_MULTIPLE_ALLOWED),

        MessageIdOption2(MessageID.submit_all_at_once_submission_policy,
            ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE),
    )

}