package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.lib.db.entities.ReportWithFilters


interface ReportDetailView: UstadDetailView<ReportWithFilters> {

    companion object {

        const val VIEW_NAME = "ReportDetailView"

    }

}