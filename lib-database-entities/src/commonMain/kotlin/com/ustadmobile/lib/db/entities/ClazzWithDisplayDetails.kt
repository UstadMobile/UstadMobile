package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ClazzWithDisplayDetails() : Clazz(){

    @Embedded
    var clazzHolidayCalendar: HolidayCalendar? = null


}