package com.ustadmobile.core.domain.clazzenrolment.pendingenrolment

import com.ustadmobile.lib.db.entities.EnrolmentRequest


interface IApproveOrDeclinePendingEnrolmentRequestUseCase {

    suspend operator fun invoke(
        enrolmentRequest: EnrolmentRequest,
        approved: Boolean,
    )
}