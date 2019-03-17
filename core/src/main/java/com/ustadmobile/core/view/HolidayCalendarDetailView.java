package com.ustadmobile.core.view;


import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.DateRange;
import com.ustadmobile.lib.db.entities.UMCalendar;

/**
 * Core View. Screen is for HolidayCalendarDetail's View
 */
public interface HolidayCalendarDetailView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "HolidayCalendarDetail";

    //Any argument keys:
    String ARG_CALENDAR_UID = "CalendarUid";

    void setListProvider(UmProvider<DateRange> umProvider);

    void updateCalendarOnView(UMCalendar calendar);

    /**
     * Method to finish the screen / view.
     */
    void finish();


}

