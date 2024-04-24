package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.CourseBlockPicture
import kotlinx.serialization.Serializable

@Serializable
data class CourseBlockAndPicture(
    @Embedded
    var block: CourseBlock? = null,
    @Embedded
    var picture: CourseBlockPicture? = null,
)
