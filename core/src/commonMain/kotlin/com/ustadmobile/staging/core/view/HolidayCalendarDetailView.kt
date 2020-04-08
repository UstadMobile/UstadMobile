package com.ustadmobile.staging.core.view


import androidx.paging.DataSource
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.DateRange
import com.ustadmobile.lib.db.entities.HolidayCalendar

/**
 * Core View. Screen is for HolidayCalendarDetail's View
 */
interface HolidayCalendarDetailView : UstadView {

    fun setListProvider(umProvider: DataSource.Factory<Int, DateRange>)

    fun updateCalendarOnView(calendar: HolidayCalendar)

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

