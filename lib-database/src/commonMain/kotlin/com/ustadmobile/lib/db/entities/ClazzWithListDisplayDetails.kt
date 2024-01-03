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

    @Embedded
    var coursePicture: CoursePicture? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClazzWithListDisplayDetails) return false
        if (!super.equals(other)) return false

        if (numStudents != other.numStudents) return false
        if (numTeachers != other.numTeachers) return false
        if (teacherNames != other.teacherNames) return false
        if (lastRecorded != other.lastRecorded) return false
        if (clazzActiveEnrolment != other.clazzActiveEnrolment) return false
        if (terminology != other.terminology) return false
        return coursePicture == other.coursePicture
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + numStudents
        result = 31 * result + numTeachers
        result = 31 * result + (teacherNames?.hashCode() ?: 0)
        result = 31 * result + lastRecorded.hashCode()
        result = 31 * result + (clazzActiveEnrolment?.hashCode() ?: 0)
        result = 31 * result + (terminology?.hashCode() ?: 0)
        result = 31 * result + (coursePicture?.hashCode() ?: 0)
        return result
    }


}
