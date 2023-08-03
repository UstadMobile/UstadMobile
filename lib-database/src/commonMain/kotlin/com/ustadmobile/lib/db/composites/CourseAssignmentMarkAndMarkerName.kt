package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import kotlinx.serialization.Serializable

@Serializable
data class CourseAssignmentMarkAndMarkerName(
    @Embedded
    var courseAssignmentMark: CourseAssignmentMark? = null,
    var markerFirstNames: String? = null,
    var markerLastName: String? = null,
)
