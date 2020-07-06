package com.ustadmobile.core.view

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.Holiday
import com.ustadmobile.lib.db.entities.HolidayCalendar

interface HolidayCalendarEditView: UstadEditView<HolidayCalendar> {

    var holidayList: DoorLiveData<List<Holiday>>?

    companion object {

        const val VIEW_NAME = "HolidayCalendarEditView"

    }

}