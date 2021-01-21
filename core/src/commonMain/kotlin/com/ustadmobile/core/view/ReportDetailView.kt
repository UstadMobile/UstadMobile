package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.util.ext.ChartData
import com.ustadmobile.lib.db.entities.ReportWithSeriesWithFilters
import com.ustadmobile.lib.db.entities.StatementEntityWithDisplay


interface ReportDetailView: UstadDetailView<ReportWithSeriesWithFilters> {

    var isAdmin: Boolean
    var statementList: List<DataSource.Factory<Int, StatementEntityWithDisplay>>?

    var chartData: ChartData?

    companion object {

        const val VIEW_NAME = "ReportDetailView"

    }

}