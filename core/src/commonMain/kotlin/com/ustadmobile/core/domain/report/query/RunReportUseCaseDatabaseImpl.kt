package com.ustadmobile.core.domain.report.query

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.PreparedStatementConfig
import com.ustadmobile.door.ext.dbType
import com.ustadmobile.door.ext.prepareAndUseStatementAsync
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.StatementReportRow
import com.ustadmobile.lib.db.composites.adapters.asStatementReportRow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 *
 */
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
        //If there are no rows in the database query result; we must use the empty subgroup
        // this might need adjusted when subgroups are by gender / known values
        val allSubGroups = this.map { it.subgroup }.distinct().ifEmpty { listOf("") }
        val datePeriod = request.reportOptions.xAxis?.datePeriod ?: return this
        val resultList = mutableListOf<StatementReportRow>()
        val rowMap = this.associateBy { Pair(it.xAxis, it.subgroup) }

        var fromDateTime = Instant
            .fromEpochMilliseconds(request.reportOptions.period.periodStartMillis(request.timeZone))
            .toLocalDateTime(request.timeZone)
        val reportEndMs = request.reportOptions.period.periodEndMillis(request.timeZone)

        while(fromDateTime.toInstant(request.timeZone).toEpochMilliseconds() < reportEndMs) {
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


    /**
     * Run the report on the database as per the request
     *
     * @return a single value flow with the report result.
     */
    override fun invoke(
        request: RunReportUseCase.RunReportRequest,
    ): Flow<RunReportUseCase.RunReportResult> {
        if(request.reportOptions.period.periodStartMillis(request.timeZone) >=
            request.reportOptions.period.periodEndMillis(request.timeZone)
        ) {
            throw IllegalArgumentException("Invalid time range: to time must be after from time")
        }

        return flow {
            val queries = generateReportQueriesUseCase(request = request, dbType = db.dbType())
            val queryResults = db.withDoorTransactionAsync {
                db.reportRunResultRowDao().deleteByReportUid(request.reportUid)
                queries.forEach { query ->
                    db.prepareAndUseStatementAsync(PreparedStatementConfig(query.sql)) { statement ->
                        query.params.forEachIndexed { index, paramVal ->
                            statement.setObject(index + 1, paramVal)
                        }
                        statement.executeUpdate()
                    }
                }

                val reportQueryResults = db.reportRunResultRowDao().getAllByReportUid(request.reportUid)
                    .groupBy { it.rqrReportSeriesUid }
                    .map {  entry ->
                        entry.key to entry.value.map {
                            it.asStatementReportRow()
                        }.fillIfNeeded(request)
                    }.toMap()

                //ensure that the order matches
                request.reportOptions.series.mapNotNull {
                    reportQueryResults[it.reportSeriesUid]
                }
            }

            emit(
                RunReportUseCase.RunReportResult(
                    timestamp = systemTimeInMillis(),
                    request = request,
                    results = queryResults
                )
            )
        }
    }
}