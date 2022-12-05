package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason

data class ClazzEnrolmentEditUiState(

    val clazzEnrolment: ClazzEnrolmentWithLeavingReason? = null,

    val roleSelectedError: String? = null,

    val startDateError: String? = null,

    val endDateError: String? = null,

    val fieldsEnabled: Boolean = true,
) {

    val leavingReasonEnabled: Boolean
        get() = clazzEnrolment?.clazzEnrolmentOutcome !=
                ClazzEnrolment.OUTCOME_IN_PROGRESS
}