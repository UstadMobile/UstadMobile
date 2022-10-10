package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ClazzWithHolidayCalendarAndSchoolAndTerminology: Clazz() {

    @Embedded
    var holidayCalendar: HolidayCalendar? = null

    @Embedded
    var school: School? = null

    @Embedded
    var terminology: CourseTerminology? = null

}