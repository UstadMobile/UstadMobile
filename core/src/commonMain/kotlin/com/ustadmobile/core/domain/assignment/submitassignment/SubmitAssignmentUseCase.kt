package com.ustadmobile.core.domain.assignment.submitassignment

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmissionAttachment

/**
 * Handle submission of an assignment - checks to ensure that the submission is valid and then stores
 * in the database. Will throw an Exception if the submission is not valid.
 */
class SubmitAssignmentUseCase {

    /**
     * @param db the system database to save in
     * @param systemImpl
     * @param assignmentUid assignment uid that is being submitted
     * @param accountPersonUid the active user who is submitting
     * @param submission the CourseAssignment the user wants to submit
     */
    @Throws(AssignmentSubmissionException::class)
    suspend operator fun invoke(
        db: UmAppDatabase,
        systemImpl: UstadMobileSystemImpl,
        assignmentUid: Long,
        accountPersonUid: Long,
        submission: CourseAssignmentSubmission,
    ) {
        db.withDoorTransactionAsync {
            val submitterUid = db.clazzAssignmentDao.getSubmitterUid(
                assignmentUid, accountPersonUid
            )

            if(submitterUid == 0L)
                throw AccountIsNotSubmitterException("not a valid submitter")

            val assignment = db.clazzAssignmentDao.findByUidAsync(assignmentUid)
                ?: throw IllegalArgumentException("Could not find assignment uid $assignmentUid")

            if(assignment.caSubmissionPolicy == ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE
                && db.courseAssignmentSubmissionDao.doesUserHaveSubmissions(accountPersonUid, assignmentUid)
            ) {
                throw AssignmentAlreadySubmittedException("Already submitted")
            }

            if(db.clazzAssignmentDao.getLatestSubmissionTimeAllowed(assignmentUid) < systemTimeInMillis()) {
                throw AssignmentDeadlinePassedException("Deadline passed!")
            }

            submission.casAssignmentUid = assignmentUid
            submission.casSubmitterUid = submitterUid
            submission.casSubmitterPersonUid = accountPersonUid
            db.courseAssignmentSubmissionDao.insertAsync(submission)
        }
    }

}