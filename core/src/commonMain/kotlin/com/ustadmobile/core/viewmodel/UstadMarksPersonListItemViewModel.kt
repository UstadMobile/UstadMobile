package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.CourseAssignmentMarkWithPersonMarker
import com.ustadmobile.lib.db.entities.CourseBlock
import kotlin.jvm.JvmInline


data class UstadMarksPersonListItemUiState(

    val mark: CourseAssignmentMarkWithPersonMarker = CourseAssignmentMarkWithPersonMarker(),

    val block: CourseBlock = CourseBlock()

)

val CourseAssignmentMarkWithPersonMarker.listItemUiState
    get() = CourseAssignmentMarkWithPersonMarkerUiState(this)

@JvmInline
value class CourseAssignmentMarkWithPersonMarkerUiState(
    val mark: CourseAssignmentMarkWithPersonMarker,
) {

    val markerGroupNameVisible: Boolean
        get() = mark.isGroup && mark.camMarkerSubmitterUid != 0L

    val camPenaltyVisible: Boolean
        get() = mark.camPenalty != 0

}