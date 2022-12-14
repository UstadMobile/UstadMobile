package com.ustadmobile.core.view

import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.core.util.ext.ChartData
import com.ustadmobile.lib.db.entities.ReportWithSeriesWithFilters
import com.ustadmobile.lib.db.entities.StatementEntityWithDisplayDetails


interface ReportDetailView: UstadDetailView<ReportWithSeriesWithFilters> {

    var saveAsTemplateVisible: Boolean
    var statementListDetails: List<DataSourceFactory<Int, StatementEntityWithDisplayDetails>>?

    var chartData: ChartData?

    companion object {

        const val VIEW_NAME = "ReportDetailView"

    }

}