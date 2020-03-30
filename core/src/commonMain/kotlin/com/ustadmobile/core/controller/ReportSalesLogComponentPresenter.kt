package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.DashboardEntryDao
import com.ustadmobile.core.db.dao.SaleDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.model.ReportOptions
import com.ustadmobile.core.util.ext.observeWithPresenter
import com.ustadmobile.core.view.CommonReportView
import com.ustadmobile.core.view.ReportTableListComponentView

import com.ustadmobile.core.view.ReportOptionsDetailView.Companion.ARG_REPORT_OPTIONS
import com.ustadmobile.lib.db.entities.ReportSalesLog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration


/**
 * Presenter for ReportSalesLogComponent view
 */
class ReportSalesLogComponentPresenter(context: Any,
                                       arguments: Map<String, String>?,
                                       view: ReportTableListComponentView)
    : CommonReportPresenter<ReportTableListComponentView>(context, arguments!!, view) {

    internal var repository: UmAppDatabase
    private var reportOptions: ReportOptions? = null
    private val entryDao: DashboardEntryDao
    internal var impl: UstadMobileSystemImpl
    internal var loggedInPersonUid: Long = 0
    private var reportOptionsString: String? = null
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
            reportOptions = json.parse(ReportOptions.serializer(), reportOptionsString!!)

            val thisP = this
            val resultLive = saleDao.getSaleLogLive()
            view.runOnUiThread(Runnable {
                resultLive.observeWithPresenter(thisP, thisP::handleSalesLogResultLive)
            })

        }

    }

    private fun handleSalesLogResultLive(result: List<ReportSalesLog>?){
        view.setSalesLogData((result as List<Any>?)!!)
    }

    override fun downloadReport() {
        view.downloadReport()
    }


}
