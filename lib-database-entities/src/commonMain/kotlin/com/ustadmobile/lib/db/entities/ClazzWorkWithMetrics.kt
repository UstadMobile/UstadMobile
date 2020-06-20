package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ClazzWorkWithMetrics() : ClazzWork() {

    var totalStudents : Int = 0

    var submittedStudents : Int = 0

    var notSubmittedStudents : Int = 0

    var completedStudents : Int = 0

    var markedStudents : Int = 0

    var firstContentEntryUid : Long = 0

    var clazzTimeZone : String? = null

    fun calculateNotSubmittedStudents(): Int {
        notSubmittedStudents = totalStudents - submittedStudents
        return notSubmittedStudents
    }


}
