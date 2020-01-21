package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.DashboardEntryDao
import com.ustadmobile.core.db.dao.SaleDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.model.ReportOptions
import com.ustadmobile.core.util.ext.observeWithPresenter
import com.ustadmobile.core.view.ReportBarChartComponentView
import com.ustadmobile.core.view.ReportOptionsDetailView.Companion.ARG_REPORT_OPTIONS
import com.ustadmobile.lib.db.entities.ReportSalesPerformance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration


/**
 * Presenter for ReportBarChartComponentView view
 */
class ReportChartViewComponentPresenter(context: Any,
                                        arguments: Map<String, String>?,
                                        view: ReportBarChartComponentView)
    : UstadBaseController<ReportBarChartComponentView>(context, arguments!!, view) {

    internal var repository: UmAppDatabase
    internal lateinit var reportOptions: ReportOptions
    internal var entryDao: DashboardEntryDao
    internal var impl: UstadMobileSystemImpl
    internal var loggedInPersonUid: Long = 0
    internal lateinit var reportOptionsString: String
    private val saleDao: SaleDao

    init {
        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        impl = UstadMobileSystemImpl.instance
        entryDao = repository.dashboardEntryDao
        saleDao = repository.saleDao
        val activeAccount = UmAccountManager.getActiveAccount(context)
        if (activeAccount != null) {
            loggedInPersonUid = activeAccount.personUid
        }
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        if (arguments.containsKey(ARG_REPORT_OPTIONS)) {
            reportOptionsString = arguments[ARG_REPORT_OPTIONS].toString()
            val json = Json(JsonConfiguration.Stable)
            reportOptions = json.parse(ReportOptions.serializer(), reportOptionsString)

            val startOfWeek = 6 //Sunday //TODO: GET THIS FROM SETTINGS, etc/
            val producerUids = ArrayList<Long>()

            val thisP = this
            GlobalScope.launch {

                val resultLive = saleDao.getSalesPerformanceReportSumGroupedByLocationLive(reportOptions.les!!,
                        producerUids, reportOptions.locations!!, reportOptions.productTypes!!,
                        reportOptions.fromDate, reportOptions.toDate,
                        reportOptions.fromPrice, reportOptions.toPrice)
                //view.runOnUiThread(Runnable {
                GlobalScope.launch(Dispatchers.Main) {
                    resultLive.observeWithPresenter(thisP, thisP::handleReportLive)
                }

            }
        }

    }


    private fun handleReportLive(result : List<ReportSalesPerformance>?){
        view.runOnUiThread(Runnable {
            view.setChartData(result as List<Any>)
        })
    }
}
