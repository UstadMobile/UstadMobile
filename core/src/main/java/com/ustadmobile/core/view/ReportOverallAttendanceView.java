package com.ustadmobile.core.view;


import java.util.LinkedHashMap;

/**
 * ReportOverallAttendance Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface ReportOverallAttendanceView extends UstadView {

    String VIEW_NAME = "ReportOverallAttendance";

    //Attendance Chart colors
    String ATTENDANCE_LINE_CHART_COLOR_MALE = "#E16173";
    String ATTENDANCE_LINE_CHART_COLOR_FEMALE = "#3969A6";
    String ATTENDANCE_LINE_CHART_COLOR_AVERAGE = "#1E6A39";


    //Attendance Chart labels
    String ATTENDANCE_LINE_MALE_LABEL_DESC = "Male";
    String ATTENDANCE_LINE_FEMALE_LABEL_DESC = "Female";
    String ATTENDANCE_LINE_AVERAGE_LABEL_DESC = "Average";

    //Attendance chart limits
    int ATTENDANCE_LINE_CHART_HEIGHT = 200;

    /**
     * Takes a map of x, y values for the chart and updates the Line chart on the View.
     * @param dataMap The hashmap containing the values
     */
    void updateAttendanceMultiLineChart(LinkedHashMap<String, LinkedHashMap<Float, Float>> dataMap,
                                        LinkedHashMap<String, LinkedHashMap<String, Float>> tableData);

    /**
     * For Android: closes the activity.
     */
    void finish();

    /**
     * Generate csv report from the View which has the table data
     */
    void generateCSVReport();

    void generateXLSXReport(String xlsxReportPath);

}
