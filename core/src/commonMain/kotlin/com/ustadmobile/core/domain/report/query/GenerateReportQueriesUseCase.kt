package com.ustadmobile.core.domain.report.query

import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.ReportSeriesYAxis
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.door.DoorDbType

class GenerateReportQueriesUseCase {

    data class ReportQueryParts2(val sql: String, val params: Array<Any>)

    private fun xAxisOrSubgroupExpression(
        field: ReportXAxis,
        dbType: Int,
    ) : String {
        val timeFieldName = "ResultSource.timestamp"
        return buildString {
            when(field) {
                ReportXAxis.DAY -> {
                    when(dbType) {
                        DoorDbType.SQLITE -> append("strftime('%d/%m/%Y', $timeFieldName/1000, 'unixepoch')")
                        DoorDbType.POSTGRES -> append("TO_CHAR(TO_TIMESTAMP($timeFieldName/1000), 'DD/MM/YYYY')")
                    }
                }

                ReportXAxis.WEEK -> {
                    when(dbType) {
                        DoorDbType.SQLITE ->
                            append("strftime('%d/%m/%Y', $field/1000, 'unixepoch', 'weekday 6', '-5 day') ")
                        DoorDbType.POSTGRES ->
                            append("TO_CHAR(DATE(DATE_TRUNC('week', TO_TIMESTAMP($field/1000))), 'DD/MM/YYYY') ")
                    }
                }

                ReportXAxis.MONTH -> {
                    when(dbType) {
                        DoorDbType.SQLITE ->
                            append("strftime('%m/%Y', $field/1000, 'unixepoch') ")
                        DoorDbType.POSTGRES ->
                            append("TO_CHAR(TO_TIMESTAMP($field/1000), 'MM/YYYY') ")
                    }
                }

                ReportXAxis.CLASS -> {
                    append("ResultSource.statementClazzUid")
                }

                ReportXAxis.GENDER -> {
                    append("COALESCE(Person.gender, 0)")
                }

                else -> TODO("Not done yet")
            }
        }
    }

    operator fun invoke(
        reportOptions: ReportOptions2,
        dbType: Int,
    ): List<ReportQueryParts2> {
        val xAxis = reportOptions.xAxis ?: throw IllegalArgumentException("null x axis")

        return reportOptions.series.map { series ->
            val yAxis = series.reportSeriesYAxis
            var sql = "SELECT "
            val paramsList = mutableListOf<Any>()

            when(yAxis) {
                null -> throw IllegalArgumentException("$series y axis is null")

                ReportSeriesYAxis.TOTAL_DURATION -> {
                    sql += "SUM(ResultSource.resultDuration)"
                }

                ReportSeriesYAxis.AVERAGE_DURATION -> {
                    sql += "SUM(ResultSource.resultDuration) /" +
                           "COUNT(DISTINCT ResultSource.contextRegistration)"
                }

                ReportSeriesYAxis.NUMBER_SESSIONS -> {
                    sql += "COUNT(DISTINCT ResultSource.contextRegistration)"
                }

                ReportSeriesYAxis.INTERACTIONS_RECORDED -> {
                    sql += "COUNT(ResultSource.statementId)"
                }

                ReportSeriesYAxis.NUMBER_ACTIVE_USERS -> {
                    sql += "COUNT(DISTINCT ResultSource.statementPersonUid)"
                }

                ReportSeriesYAxis.AVERAGE_USAGE_TIME_PER_USER -> {
                    sql += "SUM(ResultSource.resultDuration) / " +
                            "COUNT(DISTINCT ResultSource.statementPersonUid)"
                }

                ReportSeriesYAxis.NONE -> {
                    throw IllegalArgumentException("Y Axis not set")
                }
            }

            sql += " AS yAxis,\n"

            sql += xAxisOrSubgroupExpression(xAxis, dbType) + " AS xAxis\n"

            when(series.reportSeriesSubGroup) {
                ReportXAxis.NONE, null -> {
                    sql += ", '' AS subgroup\n"
                }

                else -> {
                    sql += ", ${xAxisOrSubgroupExpression(series.reportSeriesSubGroup, dbType)} AS subgroup\n"
                }
            }

            sql += "FROM StatementEntity ResultSource\n"

            if(reportOptions.xAxis.personJoinRequired ||
                series.reportSeriesSubGroup?.personJoinRequired == true
            ) {
                sql += "LEFT JOIN Person\n" +
                       "ON Person.personUid = ResultSource.statementActorPersonUid\n"
            }

            sql += " GROUP BY xAxis"

            ReportQueryParts2(sql, paramsList.toTypedArray())
        }
    }
}