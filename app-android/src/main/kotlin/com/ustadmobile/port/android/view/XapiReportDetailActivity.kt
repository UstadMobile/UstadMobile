package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.XapiReportDetailPresenter
import com.ustadmobile.core.controller.XapiReportOptions
import com.ustadmobile.core.db.dao.StatementDao
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.XapiReportDetailView
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import java.util.*


class XapiReportDetailActivity : UstadBaseActivity(), XapiReportDetailView {

    private lateinit var recyclerView: RecyclerView

    private lateinit var recyclerAdapter: XapiReportDetailAdapter

    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var presenter: XapiReportDetailPresenter

    private lateinit var floatingButton: FloatingTextButton

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

        floatingButton = findViewById(R.id.preview_fab)

        presenter = XapiReportDetailPresenter(viewContext,
                Objects.requireNonNull(UMAndroidUtil.bundleToMap(intent.extras)),
                this, UstadMobileSystemImpl.instance
        )
        presenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        floatingButton.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("Report Title")
            val container = LinearLayout(this)
            container.orientation = LinearLayout.VERTICAL
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            val dp = UMAndroidUtil.convertPixelsToDp(100f)
            lp.setMargins(dp, 0, dp, 0)
            val input = EditText(this)
            input.layoutParams = lp
            input.requestLayout()
            container.addView(input, lp)
            alertDialog.setView(container)
            alertDialog.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            alertDialog.setPositiveButton("OK") { _, _ ->
                presenter.handleAddDashboardClicked(input.text.toString())
            }
            alertDialog.show()
        }
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