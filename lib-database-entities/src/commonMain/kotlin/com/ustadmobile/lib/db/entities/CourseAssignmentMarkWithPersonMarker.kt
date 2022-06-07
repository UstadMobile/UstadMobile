package com.ustadmobile.lib.db.entities

import androidx.room.Embedded

class CourseAssignmentMarkWithPersonMarker: CourseAssignmentMark() {

    @Embedded
    var marker: Person? = null

}