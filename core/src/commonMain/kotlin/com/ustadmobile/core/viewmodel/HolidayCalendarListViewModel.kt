package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.HolidayCalendarWithNumEntries

data class HolidayCalendarListUiState(
    val holidayCalendarList: List<HolidayCalendarWithNumEntries> = listOf()
)

class HolidayCalendarListViewModel {

    companion object {

        const val DEST_NAME = "HolidayCalendarList"

    }
}