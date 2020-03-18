package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.HolidayCalendarWithNumEntries

interface HolidayCalendarListView: UstadListView<HolidayCalendar, HolidayCalendarWithNumEntries> {

    companion object {
        const val VIEW_NAME = "HolidayCalendarListView"
    }

}