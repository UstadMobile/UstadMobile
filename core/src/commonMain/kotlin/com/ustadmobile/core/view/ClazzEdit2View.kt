package com.ustadmobile.core.view

import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendar
import com.ustadmobile.lib.db.entities.Schedule

interface ClazzEdit2View: UstadEditView<ClazzWithHolidayCalendar> {

    var clazzSchedules: DoorMutableLiveData<List<Schedule>>?

    companion object {

        const val VIEW_NAME = "ClazzEdit2"

    }

}