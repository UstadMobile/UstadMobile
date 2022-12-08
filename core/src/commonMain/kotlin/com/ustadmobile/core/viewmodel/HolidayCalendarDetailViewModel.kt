package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.HolidayCalendar

data class HolidayCalendarDetailUIState(
    val holidayCalendar: HolidayCalendar? = null,
    val calendarList: List<HolidayCalendar>? = emptyList()
)