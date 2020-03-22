package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.AttendanceResultGroupedByAgeAndThreshold


/**
 * ReportNumberOfDaysClassesOpen Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface ReportAttendanceGroupedByThresholdsView : UstadView {


    fun updateTables(dataMaps: LinkedHashMap<String, List<AttendanceResultGroupedByAgeAndThreshold>>)

    /**
     * For Android: closes the activity.
     */
    fun finish()

    /**
     * Reporting : to export to CSV
     */
    fun generateCSVReport()

    fun generateXLSXReport(xlsxReportPath: String)

    companion object {

        val VIEW_NAME = "ReportAttendanceGroupedByThresholds"
    }

}
