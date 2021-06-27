package com.ustadmobile.core.view

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendarAndSchool
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.ScopedGrantAndName

interface ClazzEdit2View: UstadEditView<ClazzWithHolidayCalendarAndSchool> {

    var clazzSchedules: DoorMutableLiveData<List<Schedule>>?

    var clazzEndDateError: String?

    var clazzStartDateError: String?

    var scopedGrants: DoorLiveData<List<ScopedGrantAndName>>?

    companion object {

        const val VIEW_NAME = "ClazzEdit2"

    }

}