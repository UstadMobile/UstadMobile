package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class AssignmentProgressSummary {

    var notSubmittedStudents : Int = 0

    var submittedStudents : Int = 0

    var totalStudents: Int = 0

    var markedStudents: Int = 0

    var hasMetricsPermission: Boolean = false

    fun calculateNotSubmittedStudents(): Int {
        notSubmittedStudents = totalStudents - markedStudents - submittedStudents
        return notSubmittedStudents
    }


}