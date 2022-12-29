package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.Holiday
import com.ustadmobile.lib.db.entities.HolidayCalendar

data class HolidayCalendarEditUiState(
    val holidayCalendar: HolidayCalendar? = null,
    val fieldsEnabled: Boolean = true,
    val holidayList: List<Holiday>? = emptyList()

)