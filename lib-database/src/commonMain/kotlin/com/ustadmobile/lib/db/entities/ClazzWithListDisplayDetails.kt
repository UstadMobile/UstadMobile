package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ClazzWithListDisplayDetails() : Clazz() {

    var numStudents: Int = 0

    var numTeachers: Int = 0

    var teacherNames: String? = null

    var lastRecorded: Long = 0

    @Embedded
    var clazzActiveEnrolment: ClazzEnrolment? = null

    @Embedded
    var terminology: CourseTerminology? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ClazzWithListDisplayDetails

        if (numStudents != other.numStudents) return false
        if (numTeachers != other.numTeachers) return false
        if (teacherNames != other.teacherNames) return false
        if (lastRecorded != other.lastRecorded) return false
        if (clazzName != other.clazzName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = numStudents
        result = 31 * result + numTeachers
        result = 31 * result + (teacherNames?.hashCode() ?: 0)
        result = 31 * result + lastRecorded.hashCode()
        return result
    }


}
