package com.ustadmobile.core.view;


/**
 * Core View. Screen is for HolidayCalendarDetail's View
 */
public interface HolidayCalendarDetailView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "HolidayCalendarDetail";

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    void finish();


}

