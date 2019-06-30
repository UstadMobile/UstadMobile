package com.ustadmobile.core.view;


/**
 * Core View. Screen is for ReportSalesLogDetail's View
 */
public interface ReportSalesLogDetailView extends ReportDetailView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "ReportSalesLogDetail";

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    void finish();


}

