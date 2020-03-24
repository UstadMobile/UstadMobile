package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.HolidayCalendar

interface HolidayCalendarDoneListener  {

    fun onHolidayCalendarDone(result: List<HolidayCalendar>)

}