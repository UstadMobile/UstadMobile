package com.ustadmobile.core.view

import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.lifecycle.MutableLiveData
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar


interface SchoolDetailOverviewView: UstadDetailView<SchoolWithHolidayCalendar> {

    var schoolClazzes : DataSourceFactory<Int, ClazzWithListDisplayDetails>?

    var schoolCodeVisible: Boolean

    companion object {

        const val VIEW_NAME = "SchoolWithHolidayCalendarDetailView"

    }

}