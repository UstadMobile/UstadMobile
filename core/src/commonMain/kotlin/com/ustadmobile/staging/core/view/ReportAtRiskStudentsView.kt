package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.PersonWithEnrollment


interface ReportAtRiskStudentsView : UstadView {

    /**
     * finishes the activity.
     */
    fun finish()

    /**
     * Reporting: to export to CSV
     */
    fun generateCSVReport()

    /**
     * Starts the process of report generation and renders it to the view with the raw data
     * supplied to its argument.
     * @param dataMaps  The raw data (usually from the database via the presenter.
     */
    fun updateTables(dataMaps: LinkedHashMap<String, List<PersonWithEnrollment>>)

    fun setTableTextData(tableTextData: MutableList<Array<String>>)

    /**
     * Sets report provider to view.
     * @param provider
     */
    fun setReportProvider(provider: DataSource.Factory<Int, PersonWithEnrollment>)


    fun generateXLSXReport(xlsxReportPath: String)

    companion object {

        /**
         * The view name
         */
        val VIEW_NAME = "ReportAtRiskStudentsView"
    }
}
