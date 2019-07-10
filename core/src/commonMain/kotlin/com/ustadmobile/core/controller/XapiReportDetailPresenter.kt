package com.ustadmobile.core.controller

import com.ustadmobile.core.view.XapiReportDetailView
import com.ustadmobile.core.view.XapiReportDetailView.Companion.ARG_REPORT_OPTIONS
import kotlinx.coroutines.Runnable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

class XapiReportDetailPresenter(context: Any, arguments: Map<String, String>?, view: XapiReportDetailView)
    : UstadBaseController<XapiReportDetailView>(context, arguments!!, view) {

    private lateinit var reportOptions: XapiReportOptions

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        val json = Json(JsonConfiguration.Stable)
        val reportOptionsString = arguments.getValue(ARG_REPORT_OPTIONS)!!
        reportOptions = json.parse(XapiReportOptions.serializer(), reportOptionsString)

        //in presenter - send to the DAO

        view.runOnUiThread(Runnable { view.setChartData(reportOptions) })

    }


}