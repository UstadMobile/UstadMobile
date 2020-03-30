package com.ustadmobile.core.view

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.DateRange
import com.ustadmobile.lib.db.entities.HolidayCalendar

interface HolidayCalendarEditView: UstadEditView<HolidayCalendar> {

    var dateRangeList: DoorLiveData<List<DateRange>>?

    companion object {

        const val VIEW_NAME = "HolidayCalendarEditView"

    }

}