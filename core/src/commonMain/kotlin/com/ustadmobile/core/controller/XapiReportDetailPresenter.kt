package com.ustadmobile.core.controller

import com.ustadmobile.core.controller.XapiReportOptions.Companion.AVG_DURATION
import com.ustadmobile.core.controller.XapiReportOptions.Companion.CONTENT_ENTRY
import com.ustadmobile.core.controller.XapiReportOptions.Companion.COUNT_ACTIVITIES
import com.ustadmobile.core.controller.XapiReportOptions.Companion.DAY
import com.ustadmobile.core.controller.XapiReportOptions.Companion.DURATION
import com.ustadmobile.core.controller.XapiReportOptions.Companion.GENDER
import com.ustadmobile.core.controller.XapiReportOptions.Companion.MONTH
import com.ustadmobile.core.controller.XapiReportOptions.Companion.SCORE
import com.ustadmobile.core.controller.XapiReportOptions.Companion.WEEK
import com.ustadmobile.core.db.dao.StatementDao
import com.ustadmobile.core.db.dao.XLangMapEntryDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.XapiReportDetailView
import com.ustadmobile.core.view.XapiReportDetailView.Companion.ARG_REPORT_OPTIONS
import com.ustadmobile.door.SimpleDoorQuery
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_FEMALE
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_MALE
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_OTHER
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_UNSET
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

class XapiReportDetailPresenter(context: Any, arguments: Map<String, String>?,
                                view: XapiReportDetailView, val impl: UstadMobileSystemImpl,
                                private val statementDao: StatementDao,
                                private val xLangMapEntryDao: XLangMapEntryDao)
    : UstadBaseController<XapiReportDetailView>(context, arguments!!, view) {

    private lateinit var reportOptions: XapiReportOptions

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        val json = Json(JsonConfiguration.Stable)
        val reportOptionsString = arguments.getValue(ARG_REPORT_OPTIONS)!!
        reportOptions = json.parse(XapiReportOptions.serializer(), reportOptionsString)
        GlobalScope.launch {
            val sql = reportOptions.toSql()
            var data = statementDao.getResults(SimpleDoorQuery(sql.sqlStr, sql.queryParams))
            var time = SECS
            if (reportOptions.yAxis == DURATION || reportOptions.yAxis == AVG_DURATION) {
                time = getMeasureTime(data)
                data = changeUnitInList(data, time)
            }
            val xAxisLabel = getLabelList(reportOptions.xAxis, data.map { it.xAxis }.distinct())
            val subgroupLabel = getLabelList(reportOptions.subGroup, data.map { it.subgroup }.distinct())
            val yAxisLabel = getLabel(reportOptions.yAxis, time)
            view.runOnUiThread(Runnable {
                view.setChartData(data, reportOptions, xAxisLabel, subgroupLabel)
                view.setChartYAxisLabel(yAxisLabel)
            })
            val results = statementDao.getListResults(SimpleDoorQuery(sql.sqlListStr, sql.queryParams))
            view.runOnUiThread(Runnable {
                view.setReportListData(results)
            })

        }

    }

    fun handleAddDashboardClicked(title: String){
        reportOptions.reportTitle = title
        var args = HashMap<String, String?>()
        args[ARG_REPORT_OPTIONS] = Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptions)
        //impl.go(XapiReportDetailView.VIEW_NAME, args, context)
    }

    private fun getMeasureTime(data: List<StatementDao.ReportData>): Int {
        var units = data.maxBy { it.yAxis }
        if (units?.yAxis!! > 3600000) {
            return HRS
        } else if (units.yAxis > 60000) {
            return MINS
        }
        return SECS
    }

    private fun changeUnitInList(data: List<StatementDao.ReportData>, time: Int): List<StatementDao.ReportData> {
        data.forEach {
            when (time) {
                HRS -> it.yAxis = (it.yAxis / (1000 * 60 * 60)) % 24
                MINS -> it.yAxis = (it.yAxis / (1000 * 60)) % 60
                else -> it.yAxis = (it.yAxis / (1000)) % 60
            }
        }
        return data
    }

    private fun getLabel(value: Int, time: Int): String {
        return when (value) {
            SCORE -> impl.getString(MessageID.xapi_score, context)
            DURATION -> impl.getString(DURATION, context) + " (" + impl.getString(time, context) + ")"
            AVG_DURATION -> impl.getString(AVG_DURATION, context) + " (" + impl.getString(time, context) + ")"
            COUNT_ACTIVITIES -> impl.getString(COUNT_ACTIVITIES, context)
            else -> ""
        }
    }

    private suspend fun getLabelList(value: Int, list: List<String>): Map<String, String> {
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
                val valueList = xLangMapEntryDao.getValuesWithListOfId(list.map { it.toInt() })
                valueList.forEach {
                    mutableMap[it.objectLangMapUid.toString()] = it.valueLangMap
                }
            }
            DAY, WEEK, MONTH -> {
                list.forEach {
                    mutableMap[it] = it
                }
            }

        }
        return mutableMap.toMap()
    }

    companion object {

        const val SECS = MessageID.xapi_seconds

        const val MINS = MessageID.xapi_minutes

        const val HRS = MessageID.xapi_hours

    }


}