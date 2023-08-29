package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.MR
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.ClazzAssignment

object FileTypeConstants {

    val FILE_TYPE_MESSAGE_IDS = listOf(
        MessageIdOption2(MR.strings.file_type_any, ClazzAssignment.FILE_TYPE_ANY),
        MessageIdOption2(MR.strings.file_document, ClazzAssignment.FILE_TYPE_DOC),
        MessageIdOption2(MR.strings.file_image, ClazzAssignment.FILE_TYPE_IMAGE),
        MessageIdOption2(MR.strings.video, ClazzAssignment.FILE_TYPE_VIDEO),
        MessageIdOption2(MR.strings.audio, ClazzAssignment.FILE_TYPE_AUDIO),
    )
}