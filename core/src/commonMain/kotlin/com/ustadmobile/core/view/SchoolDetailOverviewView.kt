package com.ustadmobile.core.view

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar


interface SchoolDetailOverviewView: UstadDetailView<SchoolWithHolidayCalendar> {

    var schoolClazzes : DoorDataSourceFactory<Int, ClazzWithListDisplayDetails>?

    var schoolCodeVisible: Boolean

    companion object {

        const val VIEW_NAME = "SchoolWithHolidayCalendarDetailView"

    }

}