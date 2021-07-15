package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

//TODO: Rename this to AssignmentProgressSummary
@Serializable
class StudentAssignmentProgress {

    var notStartedStudents : Int = 0

    var startedStudents : Int = 0

    var completedStudents : Int = 0

    var totalStudents: Int = 0

    var hasMetricsPermission: Boolean = false

    fun calculateStartedStudents(): Int {
        startedStudents = totalStudents - completedStudents - notStartedStudents
        return startedStudents
    }


}