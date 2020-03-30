package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.*
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.CommonReportView
import com.ustadmobile.core.view.ReportDetailView
import com.ustadmobile.core.view.ReportOptionsDetailView.Companion.ARG_REPORT_OPTIONS
import com.ustadmobile.lib.db.entities.DashboardEntry
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import java.util.*


class ReportDetailActivity : UstadBaseActivity(), ReportDetailView {

    private var toolbar: Toolbar? = null
    private var mPresenter: ReportDetailPresenter? = null
    private var rPresenter: CommonReportPresenter<CommonReportView>? = null
    private var fab: FloatingTextButton? = null
    internal var menu: Menu? = null
    private var fabVisibility = true

    private var xLabel: TextView? = null
    private var yLabel: TextView? = null
    private var chartLL: LinearLayout? = null

    private var reportOptionsString: String? = null

    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param thisMenu  The menu options
     * @return  true. always.
     */
    override fun onCreateOptionsMenu(thisMenu: Menu): Boolean {
        menu = thisMenu
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_report_detail, menu)

        menu!!.findItem(R.id.menu_report_detail_download).isVisible = true
        menu!!.findItem(R.id.menu_report_detail_edit).isVisible = true

        return true
    }

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val i = item.itemId
        if (i == android.R.id.home) {
            onBackPressed()
            return true

        } else if (i == R.id.menu_report_detail_download) {
            mPresenter!!.handleClickDownloadReport()
            if(rPresenter!= null) {
                rPresenter!!.downloadReport()
            }
            return true
        } else if (i == R.id.menu_report_detail_edit) {
            mPresenter!!.handleClickEditReport()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_report_detail)

        //Toolbar:
        toolbar = findViewById(R.id.activity_report_sales_performance_detail_toolbar)
        toolbar!!.title = getText(R.string.sales_performance_report)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        xLabel = findViewById(R.id.activity_report_sales_perforance_detail_x_label)
        yLabel = findViewById(R.id.activity_report_sales_perforance_detail_y_label)
        chartLL = findViewById(R.id.activity_report_sales_perforance_detail_report_ll)

        xLabel!!.visibility = View.VISIBLE
        yLabel!!.visibility = View.VISIBLE

        if (intent.extras!!.containsKey(ARG_REPORT_OPTIONS)) {
            reportOptionsString = intent.extras!!.get(ARG_REPORT_OPTIONS)!!.toString()
        } else {
            reportOptionsString = ""
        }

        //Call the Presenter
        mPresenter = ReportDetailPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //FAB and its listener
        fab = findViewById(R.id.activity_report_sales_performance_detail_fab)
        fab!!.setOnClickListener { v -> mPresenter!!.handleClickAddToDashboard() }
        fab!!.visibility = if (fabVisibility) View.VISIBLE else View.INVISIBLE


    }

    override fun setTitle(title: String) {
        toolbar!!.title = title
    }

    override fun showDownloadButton(show: Boolean) {
        if (menu != null) {
            menu!!.getItem(R.id.menu_report_detail_download).isVisible = show
        }
    }

    override fun showAddToDashboardButton(show: Boolean) {
        fabVisibility = show
        runOnUiThread {
            if (fab != null) {
                fab!!.visibility = if (show) View.VISIBLE else View.INVISIBLE
            }
        }
    }

    override fun showSalesPerformanceReport() {
        //Common Layout params for chart views to match parent.
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        //Creating the args to be sent to the Chart/Report view presenter
        val args = Hashtable<String, String>()
        args[ARG_REPORT_OPTIONS] = reportOptionsString

        //Clear before adding.
        chartLL!!.removeAllViews()
        xLabel!!.visibility = View.VISIBLE
        yLabel!!.visibility = View.VISIBLE

        //Create the chart component, sets its layout and call the presenter on the view.
        val chartComponent = ReportSalesPerformanceChartComponent(this)
        chartComponent.layoutParams = params
        rPresenter = ReportChartViewComponentPresenter(this, args, chartComponent)
                as CommonReportPresenter<CommonReportView>
        rPresenter!!.onCreate(args)

        chartLL!!.addView(chartComponent)

    }


    override fun showSalesLogReport() {

        //Common Layout params for chart views to match parent.
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        //Creating the args to be sent to the Chart/Report view presenter
        val args = Hashtable<String, String>()
        args[ARG_REPORT_OPTIONS] = reportOptionsString

        //Clear before adding.
        chartLL!!.removeAllViews()

        xLabel!!.visibility = View.INVISIBLE
        yLabel!!.visibility = View.INVISIBLE

        //Create View, presenter and add it to the view
        val salesLogComponent = ReportTableListComponent(this)
        salesLogComponent.layoutParams = params
        rPresenter = ReportSalesLogComponentPresenter(this, args, salesLogComponent)
                as CommonReportPresenter<CommonReportView>
        rPresenter!!.onCreate(args)

        chartLL!!.addView(salesLogComponent)

    }

    override fun showTopLEsReport() {

        //Common Layout params for chart views to match parent.
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        //Creating the args to be sent to the Chart/Report view presenter
        val args = Hashtable<String, String>()
        args[ARG_REPORT_OPTIONS] = reportOptionsString

        //Clear before adding.
        chartLL!!.removeAllViews()

        xLabel!!.visibility = View.INVISIBLE
        yLabel!!.visibility = View.INVISIBLE

        //Create View, presenter and add it to the view
        val topLEsComponent = ReportTableListComponent(this)
        topLEsComponent.layoutParams = params
        val tPresenter = ReportTopLEsComponentPresenter(this, args, topLEsComponent)
        tPresenter.onCreate(args)

        chartLL!!.addView(topLEsComponent)

    }

    override fun setReportType(reportType: Int) {
        var title = ""
        when (reportType) {
            DashboardEntry.REPORT_TYPE_SALES_PERFORMANCE -> title = getText(R.string.sales_performance_report).toString()
            DashboardEntry.REPORT_TYPE_SALES_LOG -> title = getText(R.string.sales_log_report).toString()
            DashboardEntry.REPORT_TYPE_TOP_LES -> title = getText(R.string.top_les_report).toString()
        }
        toolbar!!.title = title
        mPresenter!!.reportTitle = title
    }


}
