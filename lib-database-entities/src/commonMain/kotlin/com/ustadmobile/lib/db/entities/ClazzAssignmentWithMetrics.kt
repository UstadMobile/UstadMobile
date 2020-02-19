package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ClazzAssignmentWithMetrics() : ClazzAssignment() {

    //eg: "30 students in total"
    var totalStudents : Int = 0

    //eg: "10 started"
    var startedStudents : Int = 0

    //eg: "5 students not started"
    var notStartedStudents : Int = 0

    //eg: "15 completed"
    var completedStudents : Int = 0

    var firstContentEntryUid : Long = 0

    //Title of the story. eg: "Story 1, Story 1C"
    var storiesTitle : String = ""

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ClazzAssignmentWithMetrics

        if (totalStudents != other.totalStudents) return false
        if (startedStudents != other.startedStudents) return false
        if (notStartedStudents != other.notStartedStudents) return false
        if (completedStudents != other.completedStudents) return false
        if (clazzAssignmentUid != other.clazzAssignmentUid) return false
        if (clazzAssignmentInstructions != other.clazzAssignmentInstructions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = totalStudents
        result = 31 * result + startedStudents
        result = 31 * result + notStartedStudents
        result = 31 * result + completedStudents
        return result
    }


}
