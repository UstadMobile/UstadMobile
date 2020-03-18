package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.HolidayCalendarWithNumEntries

interface HolidayCalendarList2View: UstadListView<HolidayCalendar, HolidayCalendarWithNumEntries> {

    companion object {
        const val VIEW_NAME = "HolidayCalendarList2"
    }

}