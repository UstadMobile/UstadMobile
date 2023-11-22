package com.ustadmobile.core.domain.assignment.submitassignment

sealed class AssignmentSubmissionException(message: String?): Exception(message)

class AccountIsNotSubmitterException(
    message: String?
): AssignmentSubmissionException(message) {
}

class AssignmentAlreadySubmittedException(
    message: String?
): AssignmentSubmissionException(message)

class AssignmentDeadlinePassedException(
    message: String?
): AssignmentSubmissionException(message)

class AssignmentTextTooLongException(
    message: String?
): AssignmentSubmissionException(message)
