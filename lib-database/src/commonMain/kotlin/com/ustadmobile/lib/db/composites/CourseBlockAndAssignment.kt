package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseBlock
import kotlinx.serialization.Serializable

@Serializable
data class CourseBlockAndAssignment(
    @Embedded
    var courseBlock: CourseBlock? = null,

    @Embedded
    var assignment: ClazzAssignment? = null,
)
