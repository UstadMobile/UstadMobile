package com.ustadmobile.core.viewmodel.clazz

import com.ustadmobile.core.MR
import com.ustadmobile.lib.db.entities.CourseBlock
import dev.icerock.moko.resources.StringResource

val CourseBlock.blockTypeStringResource: StringResource
    get() = when(cbType) {
        CourseBlock.BLOCK_TEXT_TYPE -> MR.strings.text
        CourseBlock.BLOCK_CONTENT_TYPE -> MR.strings.content
        CourseBlock.BLOCK_MODULE_TYPE -> MR.strings.module
        CourseBlock.BLOCK_ASSIGNMENT_TYPE -> MR.strings.assignment
        CourseBlock.BLOCK_DISCUSSION_TYPE -> MR.strings.discussion_board
        else -> MR.strings.blank
    }

