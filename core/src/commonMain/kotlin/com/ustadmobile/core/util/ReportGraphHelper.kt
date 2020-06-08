package com.ustadmobile.core.util

import com.ustadmobile.core.controller.XapiReportDetailPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.StatementDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.ReportFilter
import com.ustadmobile.lib.db.entities.ReportWithFilters
import com.ustadmobile.lib.db.entities.XapiReportOptions

class ReportGraphHelper(val context: Any, val impl: UstadMobileSystemImpl,val  db: UmAppDatabase) {

    suspend fun getChartDataFromReport(reportOptions: ReportWithFilters): ChartData {

        var data = db.statementDao.getResultsFromOptions(reportOptions)
        var time = XapiReportDetailPresenter.SECS
        if (reportOptions.yAxis == XapiReportOptions.DURATION || reportOptions.yAxis == XapiReportOptions.AVG_DURATION) {
            time = getMeasureTime(data)
            data = changeUnitInList(data, time)
        }
        val xAxisLabel = getLabelList(reportOptions.xAxis, data.filter { it.xAxis != null  }.map { it.xAxis }.distinct() as List<String>)
        val subgroupLabel = getLabelList(reportOptions.subGroup, data.filter { it.subgroup != null  }.map { it.subgroup }.distinct() as List<String>)
        val yAxisLabel = getLabel(reportOptions.yAxis, time)

        return ChartData(data, xAxisLabel, yAxisLabel, subgroupLabel)
    }

    suspend fun getStatementListFromReport(reportOptions: ReportWithFilters): List<StatementDao.ReportListData> {
        return db.statementDao.getResultsListFromOptions(reportOptions)
    }

    data class ChartData(val dataList: List<StatementDao.ReportData>, val xAxisLabel: Map<String, String>, val yAxisLabel: String, val subGroupLabel: Map<String, String>)

    private fun getMeasureTime(data: List<StatementDao.ReportData>): Int {
        val units = data.maxBy { it.yAxis } ?: return XapiReportDetailPresenter.SECS
        if (units.yAxis > 3600000) {
            return XapiReportDetailPresenter.HRS
        } else if (units.yAxis > 60000) {
            return XapiReportDetailPresenter.MINS
        }
        return XapiReportDetailPresenter.SECS
    }

    private fun changeUnitInList(data: List<StatementDao.ReportData>, time: Int): List<StatementDao.ReportData> {
        data.forEach {
            when (time) {
                XapiReportDetailPresenter.HRS -> it.yAxis = (it.yAxis / (1000 * 60 * 60)) % 24
                XapiReportDetailPresenter.MINS -> it.yAxis = (it.yAxis / (1000 * 60)) % 60
                else -> it.yAxis = (it.yAxis / (1000)) % 60
            }
        }
        return data
    }

    private fun getLabel(value: Int, time: Int): String {
        return when (value) {
            XapiReportOptions.SCORE -> impl.getString(MessageID.xapi_score, context)
            XapiReportOptions.DURATION -> impl.getString(XapiReportOptions.DURATION, context) + " (" + impl.getString(time, context) + ")"
            XapiReportOptions.AVG_DURATION -> impl.getString(XapiReportOptions.AVG_DURATION, context) + " (" + impl.getString(time, context) + ")"
            XapiReportOptions.COUNT_ACTIVITIES -> impl.getString(XapiReportOptions.COUNT_ACTIVITIES, context)
            else -> ""
        }
    }

    private suspend fun getLabelList(value: Int, list: List<String>): Map<String, String> {
        val mutableMap = mutableMapOf<String, String>()
        when (value) {
            XapiReportOptions.GENDER -> {
                list.forEach {
                    mutableMap[it] = when (it.toInt()) {
                        Person.GENDER_MALE -> impl.getString(MessageID.male, context)
                        Person.GENDER_FEMALE -> impl.getString(MessageID.female, context)
                        Person.GENDER_OTHER -> impl.getString(MessageID.other, context)
                        Person.GENDER_UNSET -> impl.getString(MessageID.unset, context)
                        else -> ""
                    }
                }

            }
            XapiReportOptions.CONTENT_ENTRY -> {
                val valueList = db.xLangMapEntryDao.getValuesWithListOfId(list.map { it.toInt() })
                valueList.forEach {
                    mutableMap[it.objectLangMapUid.toString()] = it.valueLangMap
                }
            }
            XapiReportOptions.DAY, XapiReportOptions.WEEK, XapiReportOptions.MONTH -> {
                list.forEach {
                    mutableMap[it] = it
                }
            }

        }
        return mutableMap.toMap()
    }

}