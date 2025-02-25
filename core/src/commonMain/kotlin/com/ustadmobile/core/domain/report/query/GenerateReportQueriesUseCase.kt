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

        /*
         * strftime should be able to use %F to create an iso formatted date, unfortunately, this
         * doesn't work across all SQLite versions, so '%Y-%m-%d' is used instead
         *
         * See https://www.sqlite.org/lang_datefunc.html
         */
        return buildString {
            when(field) {
                ReportXAxis.DAY -> {
                    when(dbType) {
                        DoorDbType.SQLITE -> append("strftime('%Y-%m-%d', $timeFieldName/1000, 'unixepoch')")
                        DoorDbType.POSTGRES -> append("TO_CHAR(TO_TIMESTAMP($timeFieldName/1000), 'YYYY-MM-DD')")
                    }
                }

                ReportXAxis.WEEK -> {
                    when(dbType) {
                        DoorDbType.SQLITE ->
                            append("strftime('%Y-%m-%d', $field/1000, 'unixepoch', 'weekday 6', '-5 day') ")
                        DoorDbType.POSTGRES ->
                            append("TO_CHAR(DATE(DATE_TRUNC('week', TO_TIMESTAMP($field/1000))), 'DD/MM/YYYY') ")
                    }
                }

                ReportXAxis.MONTH -> {
                    when(dbType) {
                        DoorDbType.SQLITE ->
                            append("strftime('%Y-%m', $field/1000, 'unixepoch') ")
                        DoorDbType.POSTGRES ->
                            append("TO_CHAR(TO_TIMESTAMP($field/1000), 'YYYY-MM') ")
                    }
                }

                ReportXAxis.CLASS -> {
                    append("ResultSource.statementClazzUid")
                }

                ReportXAxis.GENDER -> {
                    append("COALESCE(Person.gender, 0)")
                }

                ReportXAxis.NONE -> {
                    throw IllegalArgumentException("Cannot graph x axis 'none'")
                }
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
                    sql += "COUNT(*)"
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

            sql += xAxisOrSubgroupExpression(xAxis, dbType) + " AS xAxis,\n"

            when(series.reportSeriesSubGroup) {
                ReportXAxis.NONE, null -> {
                    sql += "'' AS subgroup\n"
                }

                else -> {
                    sql += "${xAxisOrSubgroupExpression(series.reportSeriesSubGroup, dbType)} AS subgroup\n"
                }
            }

            sql += "FROM StatementEntity ResultSource\n"
            sql += "WHERE ResultSource.timestamp BETWEEN ? AND ?\n"
            paramsList.add(reportOptions.timeRange.from)
            paramsList.add(reportOptions.timeRange.to)

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