package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class AssignmentProgressSummary {


    var submittedStudents : Int = 0

    var totalStudents: Int = 0

    var markedStudents: Int = 0

    /**
     * Indicates if the active user has the learner record select permission for the course,
     * e.g. they can see all submissions
     */
    var activeUserHasViewLearnerRecordsPermission: Boolean = false

    /**
     * Indicates if the assignment is done in groups. This affects the display of the summary
     * information (e.g. x students vs x groups)
     */
    var isGroupAssignment: Boolean = false

    @Deprecated("Will not be used")
    fun calculateNotSubmittedStudents(): Int {
//        notSubmittedStudents = totalStudents - markedStudents - submittedStudents
//        return notSubmittedStudents
        return 0
    }


}