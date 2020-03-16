package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class PersonWithAssignmentMetrics() : Person() {

    var startedDate : Long = 0

    var finishedDate : Long = 0

    var percentageCompleted : Double = 0.0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PersonWithAssignmentMetrics

        if (startedDate != other.startedDate) return false
        if (finishedDate != other.finishedDate) return false
        if (percentageCompleted != other.percentageCompleted) return false
        if (personUid != other.personUid) return false
        if (fullName() != other.fullName()) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + startedDate.hashCode()
        result = 31 * result + finishedDate.hashCode()
        result = 31 * result + percentageCompleted.hashCode()
        return result
    }


}
