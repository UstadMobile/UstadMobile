package com.ustadmobile.core.viewmodel.clazzassignment

import com.ustadmobile.lib.db.composites.CourseAssignmentMarkAndMarkerName
import com.ustadmobile.lib.db.entities.AverageCourseAssignmentMark
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import kotlin.math.roundToInt

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

/**
 * Get the average mark for the given list of marks. This is average of all latest unique marks
 */
fun List<CourseAssignmentMarkAndMarkerName>.averageMark(): AverageCourseAssignmentMark? {
    val latestUnique = latestUniqueMarksByMarker()

    if(latestUnique.isEmpty())
        return null

    return AverageCourseAssignmentMark().apply {
        averageScore = latestUnique.sumOf {
            it.courseAssignmentMark?.camMark?.toDouble() ?: 0.toDouble()
        }.toFloat() / latestUnique.size
        averagePenalty = (latestUnique.sumOf {
            it.courseAssignmentMark?.camPenalty?.toDouble() ?: 0.toDouble()
        } / latestUnique.size).roundToInt()
    }
}

/**
 * Check if there are any updated marks in the list (e.g. a two marks from the same marker ...
 * where a marker updated/revised their mark).
 */
fun List<CourseAssignmentMarkAndMarkerName>.hasUpdatedMarks(): Boolean {
    return groupingBy { it.courseAssignmentMark?.camMarkerSubmitterUid ?: 0L }
        .eachCount().any { it.value > 1 }
}

fun submissionStatusFor(
    markList: List<CourseAssignmentMarkAndMarkerName>,
    submissionList: List<CourseAssignmentSubmission>,
): Int {
    return when {
        markList.isNotEmpty() -> CourseAssignmentSubmission.MARKED
        submissionList.isNotEmpty() -> CourseAssignmentSubmission.SUBMITTED
        else -> CourseAssignmentSubmission.NOT_SUBMITTED
    }
}