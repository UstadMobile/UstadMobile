package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar


interface SchoolDetailOverviewView: UstadDetailView<SchoolWithHolidayCalendar> {

    var schoolClazzes : DataSource.Factory<Int, ClazzWithListDisplayDetails>?

    var schoolCodeVisible: Boolean

    companion object {

        const val VIEW_NAME = "SchoolDetailOverviewView"

    }

}