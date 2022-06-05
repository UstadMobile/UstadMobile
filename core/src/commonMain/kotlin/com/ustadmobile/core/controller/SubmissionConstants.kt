package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import kotlin.jvm.JvmField

object SubmissionConstants {

    @JvmField
    val FILE_TYPE_MAP = mapOf(
            ClazzAssignment.FILE_TYPE_ANY to MessageID.file_type_any,
            ClazzAssignment.FILE_TYPE_AUDIO to MessageID.audio,
            ClazzAssignment.FILE_TYPE_DOC to MessageID.file_document,
            ClazzAssignment.FILE_TYPE_IMAGE to MessageID.file_image,
            ClazzAssignment.FILE_TYPE_VIDEO to MessageID.video
    )

    @JvmField
    val STATUS_MAP = mapOf(
            CourseAssignmentSubmission.NOT_SUBMITTED to MessageID.not_submitted_cap,
            CourseAssignmentSubmission.SUBMITTED to MessageID.submitted_cap,
            CourseAssignmentSubmission.MARKED to MessageID.marked_cap
    )
}