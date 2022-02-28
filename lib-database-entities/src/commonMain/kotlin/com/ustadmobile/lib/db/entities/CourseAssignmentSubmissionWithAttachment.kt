package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class CourseAssignmentSubmissionWithAttachment : CourseAssignmentSubmission() {

    @Embedded
    var attachment: CourseAssignmentSubmissionAttachment? = null

}