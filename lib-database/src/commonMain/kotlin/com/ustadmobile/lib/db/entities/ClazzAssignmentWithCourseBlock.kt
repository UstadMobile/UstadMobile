package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ClazzAssignmentWithCourseBlock : ClazzAssignment() {

    @Embedded
    var block: CourseBlock? = null

}