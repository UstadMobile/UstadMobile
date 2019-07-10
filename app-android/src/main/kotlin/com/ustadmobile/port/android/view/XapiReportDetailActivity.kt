package com.ustadmobile.port.android.view

import android.os.Bundle
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.XapiReportOptions
import com.ustadmobile.core.db.dao.StatementDao
import com.ustadmobile.core.view.XapiReportDetailView

class XapiReportDetailActivity : UstadBaseActivity(), XapiReportDetailView {


    private lateinit var chartView: XapiChartView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_xapi_report_preview)

        chartView = findViewById(R.id.preview_chart_view)
    }

    override fun setChartData(chartData: List<StatementDao.ReportData>, options: XapiReportOptions) =
            chartView.setChartData(chartData, options)
}