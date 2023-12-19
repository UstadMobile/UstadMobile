package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.CourseTerminology
import kotlinx.serialization.Serializable

@Serializable
data class ClazzNameAndTerminology(
    var clazzName: String? = null,
    @Embedded
    var terminology: CourseTerminology? = null
)