package com.ustadmobile.core.viewmodel.clazzassignment

import com.ustadmobile.lib.db.composites.CourseAssignmentMarkAndMarkerName
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime

data class UstadCourseAssignmentMarkListItemUiState(

    val mark: CourseAssignmentMarkAndMarkerName = CourseAssignmentMarkAndMarkerName(),

    val localDateTimeNow: LocalDateTime,

    val dayOfWeekStrings: Map<DayOfWeek, String>,

) {

    /**
     * Where this mark is part of an assignment which is peer marked by groups, then this gives the
     * group number that provided this peer mark.
     *
     * This works without an additional database lookup: a personUid will always be greater than the
     * current time in seconds. E.g. if the submitterUid is less than 10,000 , it must be from a group
     */
    val peerGroupNumber: Int
        get() {
            val markerSubmitterUid = mark.courseAssignmentMark?.camMarkerSubmitterUid ?: Long.MAX_VALUE
            return if(markerSubmitterUid < CourseAssignmentSubmission.MIN_SUBMITTER_UID_FOR_PERSON) {
                markerSubmitterUid.toInt()
            }else {
                0
            }
        }

    val markerName: String
        get() = "${mark.markerFirstNames ?: ""} ${mark.markerLastName ?: ""}"

    val markerGroupNameVisible: Boolean
        get() = peerGroupNumber != 0

    val camPenaltyVisible: Boolean
        get() = mark.courseAssignmentMark?.camPenalty.let { it != null && it != 0f }

}
