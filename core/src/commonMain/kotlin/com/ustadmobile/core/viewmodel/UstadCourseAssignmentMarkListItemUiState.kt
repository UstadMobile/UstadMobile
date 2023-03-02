package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.CourseAssignmentMarkWithPersonMarker
import com.ustadmobile.lib.db.entities.CourseBlock


data class UstadCourseAssignmentMarkListItem(

    val mark: CourseAssignmentMarkWithPersonMarker = CourseAssignmentMarkWithPersonMarker(),

    val block: CourseBlock = CourseBlock()

) {
    val markerGroupNameVisible: Boolean
        get() = mark.isGroup && mark.camMarkerSubmitterUid != 0L

    val camPenaltyVisible: Boolean
        get() = mark.camPenalty != 0

}
