package com.ustadmobile.core.view

import com.ustadmobile.core.controller.XapiReportOptions
import com.ustadmobile.core.db.dao.StatementDao

interface XapiReportDetailView : UstadView {

    fun setChartData(chartData: List<StatementDao.ReportData>, options: XapiReportOptions)

    fun setToolbarTitle(title: String)

    companion object {

        const val VIEW_NAME = "XapiReportPreviewView"

        const val ARG_REPORT_OPTIONS = "xapireportOptions"

    }


}