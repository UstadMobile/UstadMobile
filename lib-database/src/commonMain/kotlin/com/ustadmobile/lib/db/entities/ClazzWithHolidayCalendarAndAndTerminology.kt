package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ClazzWithHolidayCalendarAndAndTerminology: Clazz() {

    @Embedded
    var holidayCalendar: HolidayCalendar? = null

    @Embedded
    var terminology: CourseTerminology? = null

    @Embedded
    var coursePicture: CoursePicture? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClazzWithHolidayCalendarAndAndTerminology) return false
        if (!super.equals(other)) return false

        if (holidayCalendar != other.holidayCalendar) return false
        if (terminology != other.terminology) return false
        return coursePicture == other.coursePicture
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (holidayCalendar?.hashCode() ?: 0)
        result = 31 * result + (terminology?.hashCode() ?: 0)
        result = 31 * result + (coursePicture?.hashCode() ?: 0)
        return result
    }


}