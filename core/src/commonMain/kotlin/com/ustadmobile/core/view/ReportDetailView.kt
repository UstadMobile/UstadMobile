package com.ustadmobile.core.view


/**
 * Core View. Screen is for ReportDetail's View
 */
interface ReportDetailView : UstadView {

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    fun setTitle(title: String)

    fun showDownloadButton(show: Boolean)

    fun showAddToDashboardButton(show: Boolean)

    fun setReportType(reportType: Int)

    fun showSalesPerformanceReport()

    fun showSalesLogReport()

    fun showTopLEsReport()

    companion object {

        // This defines the view name that is an argument value in the go() in impl.
        val VIEW_NAME = "ReportDetail"
    }

}

