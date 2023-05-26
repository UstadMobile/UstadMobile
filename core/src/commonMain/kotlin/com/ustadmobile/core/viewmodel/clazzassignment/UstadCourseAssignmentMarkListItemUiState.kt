package com.ustadmobile.core.viewmodel.clazzassignment

import com.ustadmobile.lib.db.entities.CourseAssignmentMarkWithPersonMarker
import com.ustadmobile.lib.db.entities.CourseBlock


data class UstadCourseAssignmentMarkListItemUiState(

    val mark: CourseAssignmentMarkWithPersonMarker = CourseAssignmentMarkWithPersonMarker(),

) {
    val markerGroupNameVisible: Boolean
        get() = mark.isGroup && mark.camMarkerSubmitterUid != 0L

    val camPenaltyVisible: Boolean
        get() = mark.camPenalty != 0f

}
