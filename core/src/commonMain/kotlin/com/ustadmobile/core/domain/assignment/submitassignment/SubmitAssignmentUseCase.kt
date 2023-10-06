package com.ustadmobile.core.domain.assignment.submitassignment

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.countWords
import com.ustadmobile.core.util.ext.htmlToPlainText
import com.ustadmobile.core.util.ext.lastPossibleSubmissionTime
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import io.github.aakira.napier.Napier

/**
 * Handle submission of an assignment - checks to ensure that the submission is valid and then stores
 * in the database. Will throw an Exception if the submission is not valid.
 */
class SubmitAssignmentUseCase {

    data class SubmitAssignmentResult(
        val submission: CourseAssignmentSubmission?
    )

    /**
     * @param repo the system repo to save in
     * @param assignmentUid assignment uid that is being submitted
     * @param accountPersonUid the active user who is submitting
     * @param submission the CourseAssignment the user wants to submit
     */
    @Throws(AssignmentSubmissionException::class)
    suspend operator fun invoke(
        repo: UmAppDatabase,
        submitterUid: Long,
        assignmentUid: Long,
        accountPersonUid: Long,
        submission: CourseAssignmentSubmission,
    ) : SubmitAssignmentResult {
        if(submitterUid == 0L)
            throw AccountIsNotSubmitterException("Not a valid submitter")

        val assignment = repo.clazzAssignmentDao.findByUidWithBlockAsync(assignmentUid)
            ?: throw IllegalArgumentException("Could not find assignment uid $assignmentUid")
        val courseBlock = assignment.block
            ?: throw IllegalArgumentException("Could not load courseblock")

        if(assignment.caSubmissionPolicy == ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE
            && repo.courseAssignmentSubmissionDao.doesUserHaveSubmissions(accountPersonUid, assignmentUid)
        ) {
            throw AssignmentAlreadySubmittedException("Already submitted")
        }

        if(courseBlock.lastPossibleSubmissionTime() < systemTimeInMillis()) {
            throw AssignmentDeadlinePassedException("Deadline passed!")
        }

        if(assignment.caRequireTextSubmission) {
            if(assignment.caTextLimitType == ClazzAssignment.TEXT_WORD_LIMIT &&
                (submission.casText?.htmlToPlainText()?.countWords()?: 0) > assignment.caTextLimit
            ) {
                throw AssignmentTextTooLongException("Too many words")
            }

            if(assignment.caTextLimitType == ClazzAssignment.TEXT_CHAR_LIMIT &&
                (submission.casText?.htmlToPlainText()?.length ?: 0) > assignment.caTextLimit
            ) {
                throw AssignmentTextTooLongException("Too many chars")
            }
        }

        val submissionToSave = submission.shallowCopy {
            casAssignmentUid = assignmentUid
            casSubmitterUid = submitterUid
            casSubmitterPersonUid = accountPersonUid
        }

        Napier.d("SubmitAssignmentUseCase: save to repo for submitterUid=$submitterUid assignmentUid=$assignmentUid")
        repo.courseAssignmentSubmissionDao.insertAsync(submissionToSave)

        return SubmitAssignmentResult(submissionToSave)
    }

}