package com.ustadmobile.core.view


/**
 * ReportNumberOfDaysClassesOpen Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface ReportNumberOfDaysClassesOpenView : UstadView {

    /**
     * Takes a map of x, y values for the chart and updates the Line chart on the View.
     * @param dataMap The hashmap containing the values
     */
    fun updateBarChart(dataMap: LinkedHashMap<Float, Float>)


    /**
     * For Android: closes the activity.
     */
    fun finish()

    /**
     * Generate csv report from the View which has the table data
     */
    fun generateCSVReport()

    fun generateXLSXReport(path: String)

    companion object {

        val VIEW_NAME = "ReportNumberOfDaysClassesOpen"

        //Report Bar Chart colors
        val BAR_CHART_BAR_COLOR = "#004586"

        //Label:
        val BAR_CHART_LABEL_X = "Day"
        val BAR_CHART_LABEL_Y = "# classes open"
        val BAR_LABEL = "Number of classes open"

        //Attendance chart limits
        val BAR_CHART_HEIGHT = 200
    }

}
