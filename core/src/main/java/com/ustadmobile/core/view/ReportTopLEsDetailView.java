package com.ustadmobile.core.view;


/**
 * Core View. Screen is for ReportTopLEsDetail's View
 */
public interface ReportTopLEsDetailView extends ReportDetailView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "ReportTopLEsDetail";

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    void finish();


}

