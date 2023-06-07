package com.ustadmobile.core.domain.clazzenrolment.pendingenrolment

/**
 * Students can request to join a class using a course code. This results in an enrolment with the
 * role ROLE_STUDENT_PENDING and the student is put in the pending students persongroup.
 *
 * If approved, the enrolment will be updated to ROLE_STUDENT and the student will be moved into the
 * students group. If declined, the pending enrolment will be set as inactive.
 */
interface IApproveOrDeclinePendingEnrolmentRequestUseCase {

    suspend operator fun invoke(
        personUid: Long,
        clazzUid: Long,
        approved: Boolean,
    )
}