package com.ustadmobile.core.domain.report.query

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.door.SimpleDoorQuery
import com.ustadmobile.door.ext.dbType
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.StatementReportRow
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class RunReportUseCaseDatabaseImpl(
    val db: UmAppDatabase,
    val generateReportQueriesUseCase: GenerateReportQueriesUseCase,
) : RunReportUseCase {

    /**
     * Where the XAxis is time based (day/week/month) then the list of StatementReportRow MUST
     * contain a row for each day/week/month for each subgroup. If there are no matching records in
     * the database, then the SQL query will not contain any such row.
     *
     * This functions "fills" it in with zero so the data can be graphed as expected.
     */
    private fun List<StatementReportRow>.fillIfNeeded(
        request: RunReportUseCase.RunReportRequest,
    ): List<StatementReportRow> {
        val allSubGroups = this.map { it.subgroup }.distinct()



        return when(request.reportOptions.xAxis) {
            ReportXAxis.DAY -> {
                val rowMap = this.associateBy { Pair(it.xAxis, it.subgroup) }
                val fromInstant = Instant.fromEpochMilliseconds(request.reportOptions.timeRange.from)
                val fromDateTime = fromInstant.toLocalDateTime(TimeZone.UTC)
                val toInstant = Instant.fromEpochMilliseconds(request.reportOptions.timeRange.to)
                val period = fromInstant.periodUntil(toInstant, TimeZone.UTC)

                (0 until period.days).flatMap { dayIndex ->
                    allSubGroups.map { subgroup ->
                        val dayStr = fromDateTime.date.plus(DatePeriod(days = dayIndex)).toString()
                        rowMap[Pair(dayStr, subgroup)] ?: StatementReportRow(xAxis = dayStr, subgroup = subgroup)
                    }
                }
            }
            else -> this
        }
    }


    override suspend fun invoke(
        request: RunReportUseCase.RunReportRequest,
    ): RunReportUseCase.RunReportResult {
        val queries = generateReportQueriesUseCase(request.reportOptions, db.dbType())
        return RunReportUseCase.RunReportResult(
            timestamp = systemTimeInMillis(),
            request = request,
            results = queries.map {
                db.statementDao().runReportQuery(SimpleDoorQuery(it.sql, it.params))
                    .fillIfNeeded(request)
            }
        )
    }
}