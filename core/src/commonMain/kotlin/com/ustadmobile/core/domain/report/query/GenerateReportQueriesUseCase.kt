package com.ustadmobile.core.domain.report.query

import com.ustadmobile.core.domain.report.model.ReportSeriesYAxis
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.door.DoorDbType
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toLocalDateTime

class GenerateReportQueriesUseCase {

    data class ReportQueryParts2(val sql: String, val params: Array<Any>)

    private fun xAxisOrSubgroupExpression(
        field: ReportXAxis,
        dbType: Int,
        request: RunReportUseCase.RunReportRequest,
    ) : String {
        val reportOptions = request.reportOptions
        val timeZone = request.timeZone

        /**
         * Database functions will convert ms since epoch into dates using the UTC timezone: we want
         * to get the date as per the RunReportRequest timezone. Therefor we add the timezone offset
         * such that the SQLite/Postgres functions will return the date as expected.
         *
         * This methodology doesn't handle daylight saving time changes within the report period.
         */
        val offsetMillis = timeZone.offsetAt(reportOptions.period.periodStartInstant(timeZone))
            .totalSeconds * 1000

        val timeFieldName = "(ResultSource.timestamp + $offsetMillis)"

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
                            /* Group data as documented on ReportXAxis.WEEK - on SQLite this works
                             * by :
                             *  a) When the day of week for the timestamp matches
                             *     StartOfWeekCte.TimeRangeStartDayOfWeek, just format the timestamp date
                             *  b) if not, use the weekday modifier which will advance the date
                             *     forward to when the day of week will match, then adjust backwards
                             *     7 days, thus grouping data by week commencing date.
                             */
                            append("""
                              (CASE strftime('%w', $timeFieldName/1000, 'unixepoch')
                                    WHEN (SELECT StartOfWeekCte.TimeRangeStartDayOfWeek
                                           FROM StartOfWeekCte)
                                	THEN strftime('%Y-%m-%d', $timeFieldName/1000, 'unixepoch')
                                
                                	ELSE strftime('%Y-%m-%d', $timeFieldName/1000, 
                                                  'unixepoch', 
                                                  'weekday ' || 
                                                  (SELECT StartOfWeekCte.TimeRangeStartDayOfWeek
                                                     FROM StartOfWeekCte), 
                                                  '-7 day')
                                 END)
                            """.trimIndent())
                        DoorDbType.POSTGRES -> {
                            /*
                             * Postgres DATE_TRUNC will always truncate to the last Monday. Suppose
                             * the report period starts on a Wednesday. Procedure is thus:
                             * 1) Subtract the difference number of days (2) from timestamp - a
                             *    timestamp that was Wednesday is now Monday, a timestamp that was
                             *    Thursday is now Tuesday, etc.
                             * 2) Use DATE_TRUNC('week') on adjusted timestamp - which truncates to
                             *    the last Monday.
                             * 3) Add the difference number of days back. Now both rows will have
                             *    the timestamp string for Wednesday, and can be grouped/aggregated
                             *    as expected.
                             */
                            val periodStartDayOfWeek = Instant.fromEpochMilliseconds(
                                    reportOptions.period.periodStartMillis(timeZone)
                                ).toLocalDateTime(timeZone).dayOfWeek
                            val deltaDays = periodStartDayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal
                            append("TO_CHAR(DATE_TRUNC('week', " +
                                    "TO_TIMESTAMP($timeFieldName/1000) - INTERVAL '$deltaDays days') " +
                                    "+ INTERVAL '$deltaDays days', 'YYYY-MM-DD')"
                            )
                        }

                    }
                }

                ReportXAxis.MONTH -> {
                    when(dbType) {
                        DoorDbType.SQLITE ->
                            append("strftime('%Y-%m-%d', $timeFieldName/1000, 'unixepoch', 'start of month') ")
                        DoorDbType.POSTGRES ->
                            append("TO_CHAR(DATE_TRUNC('month', TO_TIMESTAMP($timeFieldName/1000)), 'YYYY-MM-DD') ")
                    }
                }

                ReportXAxis.YEAR -> {
                    when(dbType) {
                        DoorDbType.SQLITE -> {
                            append("strftime('%Y-%m-%d', $timeFieldName/1000, 'unixepoch', 'start of year') ")
                        }
                        DoorDbType.POSTGRES -> {
                            append("TO_CHAR(DATE_TRUNC('year', TO_TIMESTAMP($timeFieldName/1000)), 'YYYY-MM-DD') ")
                        }
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
        request: RunReportUseCase.RunReportRequest,
        dbType: Int,
    ): List<ReportQueryParts2> {
        val reportOptions = request.reportOptions
        val xAxis = reportOptions.xAxis ?: throw IllegalArgumentException("null x axis")

        val reportFromMs = reportOptions.period.periodStartMillis(request.timeZone)
        val reportToMs = reportOptions.period.periodEndMillis(request.timeZone)

        return reportOptions.series.map { series ->
            val yAxis = series.reportSeriesYAxis
            var sql = ""
            val paramsList = mutableListOf<Any>()

            /*
             * Where xAxis or subgrouping is by week and we are using SQLite we need to know the
             * first day of week; so this is added as a CTE.
             */
            if(
                dbType == DoorDbType.SQLITE &&
                (xAxis == ReportXAxis.WEEK ||
                        reportOptions.series.any { it.reportSeriesSubGroup == ReportXAxis.WEEK } )
            ) {
                sql += "WITH StartOfWeekCte(TimeRangeStartDayOfWeek) AS (\n" +
                        "SELECT strftime('%w', ?, 'unixepoch') AS TimeRangeStartDayOfWeek\n" +
                        ")\n"
                paramsList.add(reportFromMs/1000)
            }

            sql += "SELECT "

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

            sql += xAxisOrSubgroupExpression(xAxis, dbType, request) + " AS xAxis,\n"

            when(series.reportSeriesSubGroup) {
                ReportXAxis.NONE, null -> {
                    sql += "'' AS subgroup\n"
                }

                else -> {
                    sql += "${xAxisOrSubgroupExpression(series.reportSeriesSubGroup, dbType, request)} AS subgroup\n"
                }
            }

            sql += "FROM StatementEntity ResultSource\n"
            sql += "WHERE ResultSource.timestamp BETWEEN ? AND ?\n"
            paramsList.add(reportFromMs)
            paramsList.add(reportToMs)

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