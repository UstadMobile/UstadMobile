package com.ustadmobile.core.view


/**
 * ReportOverallAttendance Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface ReportOverallAttendanceView : UstadView {

    /**
     * Takes a map of x, y values for the chart and updates the Line chart on the View.
     * @param dataMap The hashmap containing the values
     */
    fun updateAttendanceMultiLineChart(dataMap: LinkedHashMap<String, LinkedHashMap<Float, Float>>,
                                       tableData: LinkedHashMap<String, LinkedHashMap<String, Float>>)

    /**
     * For Android: closes the activity.
     */
    fun finish()

    /**
     * Generate csv report from the View which has the table data
     */
    fun generateCSVReport()

    fun generateXLSXReport(xlsxReportPath: String)

    companion object {

        val VIEW_NAME = "ReportOverallAttendance"

        //Attendance Chart colors
        val ATTENDANCE_LINE_CHART_COLOR_MALE = "#E16173"
        val ATTENDANCE_LINE_CHART_COLOR_FEMALE = "#3969A6"
        val ATTENDANCE_LINE_CHART_COLOR_AVERAGE = "#1E6A39"


        //Attendance Chart labels
        val ATTENDANCE_LINE_MALE_LABEL_DESC = "Male"
        val ATTENDANCE_LINE_FEMALE_LABEL_DESC = "Female"
        val ATTENDANCE_LINE_AVERAGE_LABEL_DESC = "Average"

        //Attendance chart limits
        val ATTENDANCE_LINE_CHART_HEIGHT = 200
    }

}
