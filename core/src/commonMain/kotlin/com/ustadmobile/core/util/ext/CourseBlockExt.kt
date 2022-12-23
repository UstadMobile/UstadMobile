package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity

/**
 * Icon to use in editing screens for this module. If it is a ContentEntry, it will show the
 * content type icon (e.g. video, interactive, etc). Otherwise the CourseBlock icon type should be
 * used (e.g. text, assignment, etc)
 */
val CourseBlockWithEntity.editIconId: Int
    get() = if(cbType == CourseBlock.BLOCK_CONTENT_TYPE) {
        entry?.contentTypeFlag ?: 0
    }else {
        cbType
    }

