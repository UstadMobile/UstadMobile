package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.DashboardEntryDao
import com.ustadmobile.core.db.dao.SaleDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.model.ReportOptions
import com.ustadmobile.core.view.ReportDetailView
import com.ustadmobile.core.view.ReportOptionsDetailView
import com.ustadmobile.core.view.ReportOptionsDetailView.Companion.ARG_DASHBOARD_ENTRY_UID
import com.ustadmobile.core.view.ReportOptionsDetailView.Companion.ARG_REPORT_OPTIONS
import com.ustadmobile.core.view.ReportOptionsDetailView.Companion.ARG_REPORT_TYPE
import com.ustadmobile.lib.db.entities.DashboardEntry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

/**
 * Presenter for ReportDetailView view - common for every report view.
 */
class ReportDetailPresenter(context: Any, arguments: Map<String, String?>,
                            view: ReportDetailView)
    : UstadBaseController<ReportDetailView>(context, arguments!!, view) {


    internal var repository: UmAppDatabase
    private val reportOptions: ReportOptions? = null
    private val entryDao: DashboardEntryDao
    internal var impl: UstadMobileSystemImpl
    internal var loggedInPersonUid: Long = 0
    private var reportOptionsString: String? = null
    private val saleDao: SaleDao
    internal var dashboardEntryUid: Long = 0
    var reportTitle: String? = ""
    private var reportType = 0

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

        if (arguments.containsKey(ARG_DASHBOARD_ENTRY_UID)) {
            dashboardEntryUid = arguments[ARG_DASHBOARD_ENTRY_UID].toString().toLong()
            GlobalScope.launch {
                val result = entryDao.findByUidAsync(dashboardEntryUid)
                view.runOnUiThread(Runnable {
                    view.showAddToDashboardButton(false)
                    view.setTitle(result!!.dashboardEntryTitle!!)
                })
            }

        } else {
            view.runOnUiThread(Runnable {
                view.showAddToDashboardButton(true)
            })
        }

        view.runOnUiThread(Runnable {
            view.showDownloadButton(true)
        })

        if (arguments.containsKey(ARG_REPORT_TYPE)) {
            reportType = arguments[ARG_REPORT_TYPE].toString().toInt()
            view.setReportType(reportType)
        }
        if (arguments.containsKey(ARG_REPORT_OPTIONS)) {
            reportOptionsString = arguments[ARG_REPORT_OPTIONS].toString()
        }

        view.runOnUiThread(Runnable {
            when (reportType) {
                DashboardEntry.REPORT_TYPE_SALES_PERFORMANCE -> view.showSalesPerformanceReport()
                DashboardEntry.REPORT_TYPE_SALES_LOG -> view.showSalesLogReport()
                DashboardEntry.REPORT_TYPE_TOP_LES -> view.showTopLEsReport()
                else -> {
                }
            }
        })
    }

    fun handleClickAddToDashboard() {
        val newEntry = DashboardEntry(this.reportTitle!!, reportType, loggedInPersonUid)
        newEntry.dashboardEntryReportParam = reportOptionsString
        GlobalScope.launch {
            entryDao.insertAsync(newEntry)
            view.finish()
        }
    }

    fun handleClickEditReport() {
        view.finish()
        val args = HashMap<String, String>()
        if (dashboardEntryUid != 0L)
            args.put(ARG_DASHBOARD_ENTRY_UID, dashboardEntryUid.toString())
        if (reportOptionsString != null && !reportOptionsString!!.isEmpty())
            args.put(ARG_REPORT_OPTIONS, reportOptionsString!!)
        args.put(ARG_REPORT_TYPE, reportType.toString())

        impl.go(ReportOptionsDetailView.VIEW_NAME, args, context)
    }

    fun handleClickDownloadReport() {
        //TODO:

    }
}
