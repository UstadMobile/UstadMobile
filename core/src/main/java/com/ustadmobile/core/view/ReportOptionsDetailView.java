package com.ustadmobile.core.view;


/**
 * Core View. Screen is for ReportOptionsDetail's View
 */
public interface ReportOptionsDetailView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "ReportOptionsDetail";

    //Any argument keys:
    String ARG_DASHBOARD_ENTRY_UID = "ArgDashboardEntryUid";
    String ARG_REPORT_TYPE = "ArgReportType";

    /**
     * Method to finish the screen / view.
     */
    void finish();


    void setTitle(String title);

    void setShowAverage(boolean showAverage);

    void setLocationSelected(String locationSelected);

    void setLESelected(String leSelected);

    void setGroupBySelected(String groupBySelected);

    void setProductTypeSelected(String productTypeSelected);

    void setDateRangeSelected(String dateRangeSelected);

    void setSalePriceFrom(int from);

    void setSalePriceTo(int to);

    void setGroupByPresets(String[] presets);

}

