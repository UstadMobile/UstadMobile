package com.ustadmobile.core.domain.report.query

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.core.domain.report.query.RunReportUseCase.Companion.reportQueryResultsToResultStatementReportRows
import com.ustadmobile.core.util.ext.age
import com.ustadmobile.door.PreparedStatementConfig
import com.ustadmobile.door.ext.dbType
import com.ustadmobile.door.ext.prepareAndUseStatementAsync
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 *
 */
class RunReportUseCaseDatabaseImpl(
    val db: UmAppDatabase,
    val generateReportQueriesUseCase: GenerateReportQueriesUseCase,
) : RunReportUseCase {


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
                val lastResultIsFresh = db.reportRunResultRowDao().isReportFresh(
                    reportUid = request.reportUid,
                    timeZone = request.timeZone.id,
                    freshThresholdTime = queries.first().timestamp - (request.maxFreshAge * 1000)
                )

                if(!lastResultIsFresh) {
                    db.reportRunResultRowDao().deleteByReportUidAndTimeZone(
                        request.reportUid, request.timeZone.id,
                    )
                    queries.forEach { query ->
                        db.prepareAndUseStatementAsync(PreparedStatementConfig(query.sql)) { statement ->
                            query.params.forEachIndexed { index, paramVal ->
                                try {
                                    statement.setObject(index + 1, paramVal)
                                } catch (e: IllegalArgumentException) {
                                    // Fallback to string representation if setObject fails
                                    statement.setString(index + 1, paramVal.toString())
                                }
                            }
                            statement.executeUpdate()
                        }
                    }
                }

                db.reportRunResultRowDao().getAllByReportUidAndTimeZone(
                    request.reportUid, request.timeZone.id
                )
            }

            // ClazzUids need to be looked up from the database so the user will see the name, not
            // the uid string. Other XAXis formatting (eg. dates, gender, etc) is done client side
            val isClazzUids = request.reportOptions.xAxis == ReportXAxis.CLASS

            val allClazzUids: List<Long> = if(isClazzUids) {
                queryResults.map { it.rqrXAxis.toLong() }.distinct()
            }else {
                emptyList()
            }

            //Map clazz uid longs to name as string
            val clazzNames: Map<Long, String> = db.clazzDao().findClazzNamesByUids(allClazzUids).map {
                it.clazzUid to it.clazzName
            }.toMap()

            emit(
                RunReportUseCase.RunReportResult(
                    timestamp = systemTimeInMillis(),
                    request = request,
                    results = reportQueryResultsToResultStatementReportRows(
                        queryResults = queryResults,
                        request = request,
                        xAxisNameFn = { xAxis ->
                            if(isClazzUids)
                                clazzNames[xAxis.toLong()] ?: xAxis
                            else
                                xAxis
                        }
                    ),
                    age = queryResults.age(sinceTimestamp = queries.first().timestamp)
                )
            )
        }
    }
}