package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class AverageCourseAssignmentMark {

    var averageScore: Float = 0f

    var averagePenalty: Int = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AverageCourseAssignmentMark) return false

        if (averageScore != other.averageScore) return false
        if (averagePenalty != other.averagePenalty) return false

        return true
    }

    override fun hashCode(): Int {
        var result = averageScore.hashCode()
        result = 31 * result + averagePenalty
        return result
    }


}