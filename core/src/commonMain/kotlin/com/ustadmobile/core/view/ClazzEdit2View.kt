package com.ustadmobile.core.view

import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendarAndSchool
import com.ustadmobile.lib.db.entities.Schedule

interface ClazzEdit2View: UstadEditView<ClazzWithHolidayCalendarAndSchool> {

    var clazzSchedules: DoorMutableLiveData<List<Schedule>>?

    var clazzEndDateError: String?
    var clazzStartDateError: String?

    companion object {

        const val VIEW_NAME = "ClazzEdit2"

    }

}