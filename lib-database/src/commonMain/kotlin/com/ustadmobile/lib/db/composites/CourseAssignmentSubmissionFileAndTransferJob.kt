package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmissionFile
import com.ustadmobile.lib.db.entities.TransferJobItem
import kotlinx.serialization.Serializable

@Serializable
data class CourseAssignmentSubmissionFileAndTransferJob(
    @Embedded
    var submissionFile: CourseAssignmentSubmissionFile? = null,

    @Embedded
    var transferJobItem: TransferJobItem? = null,
)
