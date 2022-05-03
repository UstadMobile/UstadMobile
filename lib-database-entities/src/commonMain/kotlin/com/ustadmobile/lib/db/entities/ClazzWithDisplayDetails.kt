package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ClazzWithDisplayDetails() : Clazz(){

    @Embedded
    var clazzHolidayCalendar: HolidayCalendar? = null

    @Embedded
    var clazzSchool: School? = null

    @Embedded
    var terminology: CourseTerminology? = null

    var numStudents: Int = 0

    var numTeachers: Int = 0

}