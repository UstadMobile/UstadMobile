package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class StudentAssignmentProgress {

    var notStartedStudents : Int = 0

    var startedStudents : Int = 0

    var completedStudents : Int = 0

    var totalStudents: Int = 0

    var hasMetricsPermission: Boolean = false

    fun calculateStartedStudents(): Int {
        startedStudents = totalStudents - completedStudents
        return startedStudents
    }


}