package com.ustadmobile.core.view

import com.ustadmobile.core.controller.XapiReportOptions
import com.ustadmobile.core.db.dao.StatementDao

interface XapiReportDetailView : UstadView {

    fun setChartData(chartData: List<StatementDao.ReportData>, options: XapiReportOptions, xAxisLabels: Map<String, String>, subgroupLabels: Map<String, String>)

    fun setChartYAxisLabel(yAxisLabel: String)

    fun setReportListData(listResults: List<StatementDao.ReportListData>)

    companion object {

        const val VIEW_NAME = "XapiReportPreviewView"

        const val ARG_REPORT_OPTIONS = "xapireportOptions"

    }


}