package com.ustadmobile.lib.db.entities

import androidx.room.Embedded

class CourseAssignmentMarkWithPersonMarker: CourseAssignmentMark() {

    var isGroup: Boolean = false

    @Embedded
    var marker: Person? = null

}