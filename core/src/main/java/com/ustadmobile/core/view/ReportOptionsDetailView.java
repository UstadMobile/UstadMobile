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
    String ARG_REPORT_OPTIONS = "ArgReportOptions";

    /**
     * Method to finish the screen / view.
     */
    void finish();


    void setTitle(String title);

    void setShowAverage(boolean showAverage);

    void setLocationSelected(String locationSelected);

    void setLESelected(String leSelected);

    void setProductTypeSelected(String productTypeSelected);

    void setDateRangeSelected(String dateRangeSelected);

    void setSalePriceRangeSelected(int from, int to, String salePriceSelected);

    void setGroupByPresets(String[] presets, int setGroupByPresets);

    void setEditMode(boolean editMode);

}

