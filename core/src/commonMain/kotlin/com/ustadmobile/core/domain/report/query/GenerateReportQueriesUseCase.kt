package com.ustadmobile.core.domain.report.query

import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.ReportSeriesYAxis
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.door.DoorDbType

class GenerateReportQueriesUseCase {

    data class ReportQueryParts2(val sql: String, val params: Array<Any>)

    private fun groupBy(
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
                    sql += "SUM(ResultSource.resultDuration) AS yAxis,"
                }

                else -> throw IllegalStateException()
            }

            sql += groupBy(xAxis, dbType) + " AS xAxis"

            when(series.reportSeriesSubGroup) {
                ReportXAxis.NONE, null -> {
                    sql += ", '' AS subgroup"
                }

                else -> {
                    sql += ", ${groupBy(series.reportSeriesSubGroup, dbType)} AS subgroup"
                }
            }

            sql += """
                FROM StatementEntity ResultSource
            """


            sql += " GROUP BY xAxis"

            ReportQueryParts2(sql, paramsList.toTypedArray())
        }
    }
}