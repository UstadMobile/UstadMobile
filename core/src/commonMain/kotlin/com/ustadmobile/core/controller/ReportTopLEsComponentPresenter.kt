package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.DashboardEntryDao
import com.ustadmobile.core.db.dao.SaleDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.model.ReportOptions
import com.ustadmobile.core.view.ReportTableListComponentView
import com.ustadmobile.core.view.ReportOptionsDetailView.Companion.ARG_REPORT_OPTIONS
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

class ReportTopLEsComponentPresenter(context: Any,
                                     arguments: Map<String, String>?,
                                     view: ReportTableListComponentView)
    : UstadBaseController<ReportTableListComponentView>(context, arguments!!, view) {

    internal var repository: UmAppDatabase
    internal lateinit var reportOptions: ReportOptions
    internal var entryDao: DashboardEntryDao
    internal var impl: UstadMobileSystemImpl
    internal var loggedInPersonUid: Long = 0
    internal var reportOptionsString: String ?= null
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
            val gson = Json(JsonConfiguration.Stable)
            reportOptions = gson.parse(ReportOptions.serializer(), reportOptionsString!!)

            GlobalScope.launch {
                val result = saleDao.getTopLEs()

                view.runOnUiThread(Runnable{ view.setTopLEsData((result as List<Any>?)!!) })
            }

        }


    }

}
