package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.CourseBlock
import kotlinx.serialization.Serializable

@Serializable
data class CourseBlockAndDisplayDetails (
    @Embedded
    var courseBlock: CourseBlock? = null,
    @Embedded
    var contentEntry: ContentEntry? = null,
    var expanded: Boolean = false,
)