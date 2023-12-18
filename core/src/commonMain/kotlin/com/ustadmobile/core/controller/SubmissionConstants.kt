package com.ustadmobile.core.controller

import com.ustadmobile.core.MR
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import kotlin.jvm.JvmField

object SubmissionConstants {

    @JvmField
    val FILE_TYPE_MAP = mapOf(
            ClazzAssignment.FILE_TYPE_ANY to MR.strings.file_type_any,
            ClazzAssignment.FILE_TYPE_AUDIO to MR.strings.audio,
            ClazzAssignment.FILE_TYPE_DOC to MR.strings.file_document,
            ClazzAssignment.FILE_TYPE_IMAGE to MR.strings.file_image,
            ClazzAssignment.FILE_TYPE_VIDEO to MR.strings.video
    )

    @JvmField
    val STATUS_MAP = mapOf(
            CourseAssignmentSubmission.NOT_SUBMITTED to MR.strings.not_submitted_cap,
            CourseAssignmentSubmission.SUBMITTED to MR.strings.submitted_cap,
            CourseAssignmentSubmission.MARKED to MR.strings.marked_cap
    )
}