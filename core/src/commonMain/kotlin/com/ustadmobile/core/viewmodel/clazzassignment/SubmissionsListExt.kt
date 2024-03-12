package com.ustadmobile.core.viewmodel.clazzassignment

import com.ustadmobile.lib.db.composites.CourseAssignmentSubmissionFileAndTransferJob
import com.ustadmobile.lib.db.composites.SubmissionAndFiles
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission

fun List<CourseAssignmentSubmission>.combineWithSubmissionFiles(
    submissionFiles: List<CourseAssignmentSubmissionFileAndTransferJob>,
) : List<SubmissionAndFiles> {
    return map {  submission ->
        SubmissionAndFiles(
            submission = submission,
            files = submissionFiles.filter {
                it.submissionFile?.casaSubmissionUid == submission.casUid
            }
        )
    }
}
