package com.ustadmobile.core.view

import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.lib.db.entities.Holiday
import com.ustadmobile.lib.db.entities.HolidayCalendar

interface HolidayCalendarEditView: UstadEditView<HolidayCalendar> {

    var holidayList: LiveData<List<Holiday>>?

    companion object {

        const val VIEW_NAME = "HolidayCalendarEditView"

    }

}