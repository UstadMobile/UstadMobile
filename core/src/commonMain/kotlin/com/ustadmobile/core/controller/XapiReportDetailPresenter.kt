package com.ustadmobile.core.controller

import com.ustadmobile.core.controller.XapiReportOptions.Companion.CONTENT_ENTRY
import com.ustadmobile.core.controller.XapiReportOptions.Companion.GENDER
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.XapiReportDetailView
import com.ustadmobile.core.view.XapiReportDetailView.Companion.ARG_REPORT_OPTIONS
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_FEMALE
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_MALE
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_OTHER
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_UNSET
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

        val json = Json(JsonConfiguration.Stable)
        val reportOptionsString = arguments.getValue(ARG_REPORT_OPTIONS)!!
        reportOptions = json.parse(XapiReportOptions.serializer(), reportOptionsString)

        GlobalScope.launch {
            var data = db.statementDao.findThis()
            var xAxisLabel = getLabelList(reportOptions.xAxis, data.map { it.xAxis }.distinct())
            var subgroupLabel = getLabelList(reportOptions.subGroup, data.map { it.subgroup }.distinct())
            view.runOnUiThread(Runnable {
                view.setChartData(data, reportOptions, xAxisLabel, subgroupLabel)
            })
        }

    }

    suspend fun getLabelList(value: Int, list: List<String>): Map<String, String> {
        val mutableMap = mutableMapOf<String, String>()
        when (value) {
            GENDER -> {
                list.forEach {
                    mutableMap[it] = when (it.toInt()) {
                        GENDER_MALE -> impl.getString(MessageID.male, context)
                        GENDER_FEMALE -> impl.getString(MessageID.female, context)
                        GENDER_OTHER -> impl.getString(MessageID.other, context)
                        GENDER_UNSET -> impl.getString(MessageID.unset, context)
                        else -> ""
                    }
                }

            }
            CONTENT_ENTRY -> {
                var valueList = db.xLangMapEntryDao.getValuesWithListOfId(list.map { it.toInt() })
                valueList.forEach {
                    mutableMap[it.objectLangMapUid.toString()] = it.valueLangMap
                }
            }
        }



        return mutableMap.toMap()
    }


}