package com.ustadmobile.lib.db.composites

import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmissionFile
import kotlinx.serialization.Serializable

@Serializable
class SubmissionAndAttachments(
    val submission: CourseAssignmentSubmission,
    val attachments: List<CourseAssignmentSubmissionFile>
)

