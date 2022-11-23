package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.lib.db.entities.ClazzAssignment

object SubmissionConstants {

    val SUBMISSION_POLICY_OPTIONS = mapOf(
        ClazzAssignment.SUBMISSION_POLICY_MULTIPLE_ALLOWED to MessageID.multiple_submission_allowed_submission_policy,
        ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE to MessageID.submit_all_at_once_submission_policy)

}