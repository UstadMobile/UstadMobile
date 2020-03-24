package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ClazzWithHolidayCalendar: Clazz() {

    @Embedded
    var holidayCalendar: HolidayCalendar? = null
}