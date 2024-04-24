package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.CourseBlockPicture
import kotlinx.serialization.Serializable

@Serializable
data class CourseBlockAndDisplayDetails (
    @Embedded
    var courseBlock: CourseBlock? = null,
    @Embedded
    var courseBlockPicture: CourseBlockPicture? = null,
    @Embedded
    var contentEntry: ContentEntry? = null,
    var expanded: Boolean = false,
)