package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.util.ReportGraphHelper
import com.ustadmobile.lib.db.entities.ReportWithSeriesWithFilters
import com.ustadmobile.lib.db.entities.StatementListReport


interface ReportDetailView: UstadDetailView<ReportWithSeriesWithFilters> {

    var statementList: DataSource.Factory<Int, StatementListReport>?

    var chartData: ReportGraphHelper.ChartData?

    companion object {

        const val VIEW_NAME = "ReportDetailView"

    }

}