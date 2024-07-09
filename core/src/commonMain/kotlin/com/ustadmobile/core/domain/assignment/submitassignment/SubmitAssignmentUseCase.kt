package com.ustadmobile.core.domain.assignment.submitassignment

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.countWords
import com.ustadmobile.core.util.ext.htmlToPlainText
import com.ustadmobile.core.util.ext.lastPossibleSubmissionTime
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import io.github.aakira.napier.Napier
import com.ustadmobile.core.MR
import com.ustadmobile.lib.db.entities.ext.shallowCopy

/**
 * Handle submission of an assignment - checks to ensure that the submission is valid and then stores
 * in the database. Will throw an Exception if the submission is not valid.
 */
class SubmitAssignmentUseCase(
    private val systemImpl: UstadMobileSystemImpl,
) {

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

        val assignmentAndBlock = repo.clazzAssignmentDao().findByUidWithBlockAsync(assignmentUid)
            ?: throw IllegalArgumentException("Could not find assignment uid $assignmentUid")
        val courseBlock = assignmentAndBlock.block
            ?: throw IllegalArgumentException("Could not load courseblock")
        val assignment = assignmentAndBlock.assignment
            ?: throw IllegalArgumentException("assignment cannot be null")

        if(assignment.caSubmissionPolicy == ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE
            && repo.courseAssignmentSubmissionDao().doesUserHaveSubmissions(accountPersonUid, assignmentUid)
        ) {
            throw AssignmentAlreadySubmittedException(systemImpl.getString(MR.strings.already_submitted))
        }

        if(courseBlock.lastPossibleSubmissionTime() < systemTimeInMillis()) {
            throw AssignmentDeadlinePassedException(systemImpl.getString(MR.strings.deadline_has_passed))
        }

        if(assignment.caRequireTextSubmission) {
            if(assignment.caTextLimitType == ClazzAssignment.TEXT_WORD_LIMIT) {
                val wordCount = (submission.casText?.htmlToPlainText()?.countWords()?: 0)
                if(wordCount > assignment.caTextLimit) {
                    throw AssignmentTextTooLongException(systemImpl.formatString(
                        MR.strings.exceeds_word_limit, wordCount.toString(),
                        assignment.caTextLimit.toString()))
                }
            }else if(assignment.caTextLimitType == ClazzAssignment.TEXT_CHAR_LIMIT) {
                val charCount = (submission.casText?.htmlToPlainText()?.length ?: 0)
                if(charCount > assignment.caTextLimit) {
                    throw AssignmentTextTooLongException(systemImpl.formatString(
                        MR.strings.exceeds_char_limit, charCount.toString(),
                        assignment.caTextLimit.toString()))
                }
            }
        }

        val submissionToSave = submission.shallowCopy {
            casAssignmentUid = assignmentUid
            casSubmitterUid = submitterUid
            casSubmitterPersonUid = accountPersonUid
            casTimestamp = systemTimeInMillis()
            casClazzUid = assignment.caClazzUid
        }

        Napier.d("SubmitAssignmentUseCase: save to repo for submitterUid=$submitterUid assignmentUid=$assignmentUid")
        repo.courseAssignmentSubmissionDao().insertAsync(submissionToSave)

        return SubmitAssignmentResult(submissionToSave)
    }

}