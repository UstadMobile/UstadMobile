package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.ClazzLog;

import java.util.LinkedHashMap;

/**
 * ClassLogList Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface ClassLogListView extends UstadView {

    //View name
    String VIEW_NAME = "ClassLogList";

    //Attendance Chart colors
    String ATTENDANCE_LINE_CHART_COLOR = "#004586";

    //Attendance Chart labels
    String ATTENDANCE_LINE_LABEL_DESC = "Line 1 Label";
    String ATTENDANCE_BAR_LABEL_DESC = "Bar 1 Label";

    //Attendance chart limits
    int ATTENDANCE_LINE_CHART_HEIGHT = 100;
    //int ATTENDANCE_BAR_CHART_HEIGHT = 78;
    int ATTENDANCE_BAR_CHART_HEIGHT = 100;
    int ATTENDANCE_BAR_CHART_AXIS_MAXIMUM = 100;
    int ATTENDANCE_BAR_CHART_AXIS_MINIMUM = 0;

    //Attendance chart time duration flags.
    int CHART_DURATION_LAST_WEEK = 1;
    int CHART_DURATION_LAST_MONTH = 2;
    int CHART_DURATION_LAST_YEAR = 3;


    /**
     * This method's purpose is to set the provider given to it to the view.
     * On Android it will be set to the recycler view..
     *
     * @param clazzLogListProvider The provider data
     */
    void setClazzLogListProvider(UmProvider<ClazzLog> clazzLogListProvider);

    /**
     * Takes a map of x, y values for the chart and updates the Line chart on the View.
     * @param dataMap The hashmap containing the values
     */
    void updateAttendanceLineChart(LinkedHashMap<Float, Float> dataMap);

    /**
     * Takes a map of x, y values for the chart and updates the bar chart on the View.
     * @param dataMap the Hashmap containing the values.
     */
    void updateAttendanceBarChart(LinkedHashMap<Float, Float> dataMap);

    /**
     * Reset report buttons color
     */
    void resetReportButtons();

    /**
     * Sets visibility of the "Take Attendance" button
     * @param visible   true if the FAB is to bw shown
     */
    void setFABVisibility(boolean visible);

    /**
     * Shows a message to view. Can be as simple as toast on Android
     * @param messageID   The message ID we want to show.
     */
    void showMessage(int messageID);

}
