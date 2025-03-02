package com.ustadmobile.core.domain.report.query

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.SimpleDoorQuery
import com.ustadmobile.door.ext.dbType
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.StatementReportRow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

class RunReportUseCaseDatabaseImpl(
    val db: UmAppDatabase,
    val generateReportQueriesUseCase: GenerateReportQueriesUseCase,
) : RunReportUseCase {

    /**
     * Where the XAxis is time based (day/week/month/year) then the list of StatementReportRow MUST
     * contain a row for each day/week/month for each subgroup. If there are no matching records in
     * the database, then the SQL query will not contain any such row.
     *
     * This functions "fills" it in with zero so the data can be graphed as expected.
     */
    private fun List<StatementReportRow>.fillIfNeeded(
        request: RunReportUseCase.RunReportRequest,
    ): List<StatementReportRow> {
        val allSubGroups = this.map { it.subgroup }.distinct()
        val datePeriod = request.reportOptions.xAxis?.datePeriod ?: return this
        val resultList = mutableListOf<StatementReportRow>()
        val timezone = TimeZone.UTC
        val rowMap = this.associateBy { Pair(it.xAxis, it.subgroup) }

        var fromDateTime = Instant
            .fromEpochMilliseconds(request.reportOptions.timeRange.periodStartMillis(timezone))
            .toLocalDateTime(timezone)
        val reportEndMs = request.reportOptions.timeRange.periodEndMillis(timezone)

        while(fromDateTime.toInstant(timezone).toEpochMilliseconds() < reportEndMs) {
            val xAxisStr = fromDateTime.date.toString()
            resultList.addAll(
                allSubGroups.map { subgroup ->
                    rowMap[Pair(xAxisStr, subgroup)] ?: StatementReportRow(xAxis = xAxisStr, subgroup = subgroup)
                }
            )

            fromDateTime = LocalDateTime(fromDateTime.date.plus(datePeriod), fromDateTime.time)
        }

        return resultList.toList()
    }


    override suspend fun invoke(
        request: RunReportUseCase.RunReportRequest,
    ): RunReportUseCase.RunReportResult {
        if(request.reportOptions.timeRange.periodStartMillis(request.timeZone) >=
            request.reportOptions.timeRange.periodEndMillis(request.timeZone)) {
            throw IllegalArgumentException("Invalid time range: to time must be after from time")
        }

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