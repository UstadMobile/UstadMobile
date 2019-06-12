package com.ustadmobile.core.view;


/**
 * Core View. Screen is for SelectDateRangeDialog's View
 */
public interface SelectDateRangeDialogView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "SelectDateRangeDialog";

    //Any argument keys:
    String ARG_FROM_DATE = "ArgFromDate";
    String ARG_TO_DATE = "ArgToDate";

    /**
     * Method to finish the screen / view.
     */
    void finish();


}

