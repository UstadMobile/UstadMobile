package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.XapiReportDetailPresenter
import com.ustadmobile.core.controller.XapiReportOptions
import com.ustadmobile.core.db.dao.StatementDao
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.XapiReportDetailView
import java.util.*





class XapiReportDetailActivity : UstadBaseActivity(), XapiReportDetailView {

    private lateinit var recyclerView: RecyclerView

    private lateinit var recyclerAdapter: XapiReportDetailAdapter

    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var presenter: XapiReportDetailPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_xapi_report_preview)

        setUMToolbar(R.id.preview_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        umToolbar.title = getString(R.string.activity_preview_xapi)

        recyclerView = findViewById(R.id.preview_report_list)
        viewManager = LinearLayoutManager(this)
        recyclerView.layoutManager = viewManager

        recyclerAdapter = XapiReportDetailAdapter(this)
        recyclerView.adapter = recyclerAdapter

        presenter = XapiReportDetailPresenter(viewContext,
                Objects.requireNonNull(UMAndroidUtil.bundleToMap(intent.extras)),
                this)
        presenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))
    }

    override fun setChartYAxisLabel(yAxisLabel: String) {
        recyclerAdapter.showVerticalTextView(yAxisLabel)
    }

    override fun setChartData(chartData: List<StatementDao.ReportData>, options: XapiReportOptions,
                              xAxisLabels: Map<String, String>, subgroupLabels: Map<String, String>) =
            recyclerAdapter.showChart(chartData, options, xAxisLabels, subgroupLabels)


    override fun setReportListData(listResults: List<StatementDao.ReportListData>) {
        /*   val data = LivePagedListBuilder(listResults, 20).build()
           data.observe(this, Observer<PagedList<StatementDao.ReportListData>> { recyclerAdapter!!.submitList(it) })

           recyclerView.adapter = recyclerAdapter*/
        recyclerAdapter.submitList(listResults)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                runOnUiThread { finish() }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}