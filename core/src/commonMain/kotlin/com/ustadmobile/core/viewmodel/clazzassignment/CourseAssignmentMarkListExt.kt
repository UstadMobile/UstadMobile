package com.ustadmobile.core.viewmodel.clazzassignment

import com.ustadmobile.lib.db.composites.CourseAssignmentMarkAndMarkerName

/**
 * Given a list of all marks for a given submitter, get a list of the most recent marks by each
 * marker e.g. where an assignment is peer marked - get the latest mark from each peer marking.
 * Where it is marked by the teacher, this will just give the one latest mark
 */
fun List<CourseAssignmentMarkAndMarkerName>.latestUniqueMarksByMarker(

) : List<CourseAssignmentMarkAndMarkerName> = filter { markWithMarker ->
    val mostRecentTsForSubmitterUid = this.filter {
        it.courseAssignmentMark?.camMarkerSubmitterUid == markWithMarker.courseAssignmentMark?.camMarkerSubmitterUid
    }.maxOf { it.courseAssignmentMark?.camLct ?: 0 }

    markWithMarker.courseAssignmentMark?.camLct ==mostRecentTsForSubmitterUid

}