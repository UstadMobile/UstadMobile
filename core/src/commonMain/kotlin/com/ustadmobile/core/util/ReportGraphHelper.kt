package com.ustadmobile.core.util

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.StatementDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.*

class ReportGraphHelper(val context: Any, val impl: UstadMobileSystemImpl,val  db: UmAppDatabase) {

    suspend fun getChartDataForReport(reportOptions: ReportWithSeriesWithFilters): ChartData {

        var data = db.statementDao.getResultsFromOptions(reportOptions)
        var time = SECS
       /* if (reportOptions.yAxis == Report.DURATION || reportOptions.yAxis == Report.AVG_DURATION) {
            time = getMeasureTime(data)
            data = changeUnitInList(data, time)
        }*/
        val xAxisLabel = getLabelList(reportOptions.xAxis, data.filter { it.xAxis != null  }.map { it.xAxis }.distinct() as List<String>)
        val subgroupLabel = mapOf("" to "") /*getLabelList(reportOptions.subGroup, data.filter { it.subgroup != null  }.map { it.subgroup }.distinct() as List<String>) */
        val yAxisLabel = "" /*getLabel(reportOptions.yAxis, time) */

        return ChartData(data, xAxisLabel, yAxisLabel, subgroupLabel, reportOptions)
    }

    suspend fun getStatementListForReport(reportOptions: ReportWithSeriesWithFilters): DataSource.Factory<Int, StatementListReport> {
        return db.statementDao.getResultsListFromOptions(reportOptions)
    }

    data class ChartData(val dataList: List<StatementDao.ReportData>, val xAxisLabel: Map<String, String>, val yAxisLabel: String, val subGroupLabel: Map<String, String>, val reportWithFilters: ReportWithSeriesWithFilters)

    private fun getMeasureTime(data: List<StatementDao.ReportData>): Int {
        val units = data.maxBy { it.yAxis } ?: return SECS
        if (units.yAxis > 3600000) {
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
            Report.SCORE -> impl.getString(MessageID.xapi_score, context)
            Report.DURATION -> impl.getString(MessageID.total_duration, context) + " (" + impl.getString(time, context) + ")"
            Report.AVG_DURATION -> impl.getString(MessageID.average_duration, context) + " (" + impl.getString(time, context) + ")"
            Report.COUNT_ACTIVITIES -> impl.getString(MessageID.count_activity, context)
            else -> ""
        }
    }

    private suspend fun getLabelList(value: Int, list: List<String>): Map<String, String> {
        val mutableMap = mutableMapOf<String, String>()
        when (value) {
            Report.GENDER -> {
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
            Report.CONTENT_ENTRY -> {
                val valueList = db.xLangMapEntryDao.getValuesWithListOfId(list.map { it.toInt() })
                valueList.forEach {
                    mutableMap[it.objectLangMapUid.toString()] = it.valueLangMap
                }
            }
            Report.DAY, Report.WEEK, Report.MONTH -> {
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