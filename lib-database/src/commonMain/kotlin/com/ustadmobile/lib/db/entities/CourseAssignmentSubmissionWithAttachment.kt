package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class CourseAssignmentSubmissionWithAttachment : CourseAssignmentSubmission() {

    @Embedded
    var attachment: CourseAssignmentSubmissionFile? = null

}

val CourseAssignmentSubmissionWithAttachment.displayTitle: String
    get() {
        if(casType == CourseAssignmentSubmission.SUBMISSION_TYPE_TEXT) {
            return casText ?: ""
        }else {
            return attachment?.casaFileName ?: ""
        }
    }