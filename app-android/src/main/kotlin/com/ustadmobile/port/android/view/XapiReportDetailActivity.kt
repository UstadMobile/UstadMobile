package com.ustadmobile.port.android.view

import android.os.Bundle
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.XapiReportDetailPresenter
import com.ustadmobile.core.controller.XapiReportOptions
import com.ustadmobile.core.db.dao.StatementDao
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.XapiReportDetailView
import java.util.*

class XapiReportDetailActivity : UstadBaseActivity(), XapiReportDetailView {


    private lateinit var chartView: XapiChartView

    private lateinit var presenter: XapiReportDetailPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_xapi_report_preview)

        chartView = findViewById(R.id.preview_chart_view)

        presenter = XapiReportDetailPresenter(viewContext,
                Objects.requireNonNull(UMAndroidUtil.bundleToMap(intent.extras)),
                this)
        presenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))
    }

    override fun setChartData(chartData: List<StatementDao.ReportData>, options: XapiReportOptions) =
            chartView.setChartData(chartData, options)
}