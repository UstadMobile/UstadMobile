package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.lib.db.entities.ReportWithFilters


interface ReportListView: UstadListView<Report, Report> {

    companion object {
        const val VIEW_NAME = "ReportListView"
    }

}