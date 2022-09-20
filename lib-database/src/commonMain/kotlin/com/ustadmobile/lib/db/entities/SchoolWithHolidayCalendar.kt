package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class SchoolWithHolidayCalendar: School() {

    @Embedded
    var holidayCalendar: HolidayCalendar? = null
}