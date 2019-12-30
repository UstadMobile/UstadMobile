package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzLogWithScheduleStartEndTimes

/**
 * ClassLogList Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface ClassLogListView : UstadView {


    /**
     * This method's purpose is to set the provider given to it to the view.
     * On Android it will be set to the recycler view..
     *
     * @param clazzLogListProvider The provider data
     */
    fun setClazzLogListProvider(clazzLogListProvider: DataSource.Factory<Int, ClazzLogWithScheduleStartEndTimes>)

    /**
     * Takes a map of x, y values for the chart and updates the Line chart on the View.
     * @param dataMap The hashmap containing the values
     */
    fun updateAttendanceLineChart(dataMap: LinkedHashMap<Float, Float>)

    /**
     * Takes a map of x, y values for the chart and updates the bar chart on the View.
     * @param dataMap the Hashmap containing the values.
     */
    fun updateAttendanceBarChart(dataMap: LinkedHashMap<Float, Float>)

    /**
     * Reset report buttons color
     */
    fun resetReportButtons()

    /**
     * Sets visibility of the "Take Attendance" button
     * @param visible   true if the FAB is to bw shown
     */
    fun setFABVisibility(visible: Boolean)

    /**
     * Shows a message to view. Can be as simple as toast on Android
     * @param messageID   The message ID we want to show.
     */
    fun showMessage(messageID: Int)

    fun showMessage(message: String)

    companion object {

        //View name
        val VIEW_NAME = "ClassLogList"

        //Attendance Chart colors
        val ATTENDANCE_LINE_CHART_COLOR = "#004586"

        //Attendance Chart labels
        val ATTENDANCE_LINE_LABEL_DESC = "Line 1 Label"
        val ATTENDANCE_BAR_LABEL_DESC = "Bar 1 Label"

        //Attendance chart limits
        val ATTENDANCE_LINE_CHART_HEIGHT = 100
        //int ATTENDANCE_BAR_CHART_HEIGHT = 78;
        val ATTENDANCE_BAR_CHART_HEIGHT = 100
        val ATTENDANCE_BAR_CHART_AXIS_MAXIMUM = 100
        val ATTENDANCE_BAR_CHART_AXIS_MINIMUM = 0

        //Attendance chart time duration flags.
        val CHART_DURATION_LAST_WEEK = 1
        val CHART_DURATION_LAST_MONTH = 2
        val CHART_DURATION_LAST_YEAR = 3
    }

}
