package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ReportMasterItem

interface ReportMasterView : UstadView {
    fun finish()
    fun generateCSVReport()
    fun updateTables(items: List<ReportMasterItem>)

    fun generateXLSXReport(xlsxReportPath: String)

    companion object {
        val VIEW_NAME = "ReportMasterView"
    }
}
