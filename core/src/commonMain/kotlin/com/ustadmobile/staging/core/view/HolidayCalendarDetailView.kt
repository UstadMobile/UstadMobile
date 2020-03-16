package com.ustadmobile.core.view


import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.DateRange
import com.ustadmobile.lib.db.entities.UMCalendar

/**
 * Core View. Screen is for HolidayCalendarDetail's View
 */
interface HolidayCalendarDetailView : UstadView {

    fun setListProvider(umProvider: DataSource.Factory<Int, DateRange>)

    fun updateCalendarOnView(calendar: UMCalendar)

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        val VIEW_NAME = "HolidayCalendarDetail"

        //Any argument keys:
        val ARG_CALENDAR_UID = "CalendarUid"
    }


}

