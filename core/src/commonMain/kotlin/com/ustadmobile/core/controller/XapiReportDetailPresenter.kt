package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.XapiReportDetailView
import com.ustadmobile.core.view.XapiReportDetailView.Companion.ARG_REPORT_OPTIONS
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

class XapiReportDetailPresenter(context: Any, arguments: Map<String, String>?, view: XapiReportDetailView)
    : UstadBaseController<XapiReportDetailView>(context, arguments!!, view) {

    private lateinit var impl: UstadMobileSystemImpl
    private lateinit var db: UmAppDatabase

    private lateinit var reportOptions: XapiReportOptions

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        db = UmAccountManager.getRepositoryForActiveAccount(context)
        impl = UstadMobileSystemImpl.instance

        view.runOnUiThread(Runnable { view.setToolbarTitle(impl.getString(MessageID.activity_preview_xapi, context)) })

        val json = Json(JsonConfiguration.Stable)
        val reportOptionsString = arguments.getValue(ARG_REPORT_OPTIONS)!!
        reportOptions = json.parse(XapiReportOptions.serializer(), reportOptionsString)

        GlobalScope.launch {
            var data = db.statementDao.findThis()
            view.runOnUiThread(Runnable { view.setChartData(data, reportOptions) })
        }

    }


}