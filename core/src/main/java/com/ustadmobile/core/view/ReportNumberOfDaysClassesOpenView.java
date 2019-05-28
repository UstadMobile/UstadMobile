package com.ustadmobile.core.view;


import java.util.LinkedHashMap;

/**
 * ReportNumberOfDaysClassesOpen Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface ReportNumberOfDaysClassesOpenView extends UstadView {

    String VIEW_NAME = "ReportNumberOfDaysClassesOpen";

    //Report Bar Chart colors
    String BAR_CHART_BAR_COLOR = "#004586";

    //Label:
    String BAR_CHART_LABEL_X = "Day";
    String BAR_CHART_LABEL_Y = "# classes open";
    String BAR_LABEL = "Number of classes open";

    //Attendance chart limits
    int BAR_CHART_HEIGHT = 200;

    /**
     * Takes a map of x, y values for the chart and updates the Line chart on the View.
     * @param dataMap The hashmap containing the values
     */
    void updateBarChart(LinkedHashMap<Float, Float> dataMap);


    /**
     * For Android: closes the activity.
     */
    void finish();

    /**
     * Generate csv report from the View which has the table data
     */
    void generateCSVReport();

    void generateXLSXReport(String path);

}
