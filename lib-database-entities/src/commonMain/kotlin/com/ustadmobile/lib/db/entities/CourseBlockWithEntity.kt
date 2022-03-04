package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class CourseBlockWithEntity: CourseBlock() {

    @Embedded
    var assignment: ClazzAssignment? = null

    @Embedded
    var entry: ContentEntry? = null

}