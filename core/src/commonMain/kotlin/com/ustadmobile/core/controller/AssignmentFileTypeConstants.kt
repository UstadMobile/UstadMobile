package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.lib.db.entities.ClazzAssignment
import kotlin.jvm.JvmField

object AssignmentFileTypeConstants {

    @JvmField
    val FILE_TYPE_MAP = mapOf(
            ClazzAssignment.FILE_TYPE_ANY to MessageID.file_type_any,
            ClazzAssignment.FILE_TYPE_AUDIO to MessageID.audio,
            ClazzAssignment.FILE_TYPE_DOC to MessageID.file_document,
            ClazzAssignment.FILE_TYPE_IMAGE to MessageID.file_image,
            ClazzAssignment.FILE_TYPE_VIDEO to MessageID.video
    )
}