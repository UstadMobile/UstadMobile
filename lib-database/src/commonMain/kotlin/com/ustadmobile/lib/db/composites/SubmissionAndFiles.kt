package com.ustadmobile.lib.db.composites

import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import kotlinx.serialization.Serializable

@Serializable
class SubmissionAndFiles(
    val submission: CourseAssignmentSubmission,
    val files: List<CourseAssignmentSubmissionFileAndTransferJob>
)

