package com.ustadmobile.core.domain.report.query

import com.ustadmobile.core.db.UmAppDatabase
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
                    freshThresholdTime = queries.first().timestamp - (request.maxFreshAge * 1000)
                )

                if(!lastResultIsFresh) {
                    db.reportRunResultRowDao().deleteByReportUid(request.reportUid)
                    queries.forEach { query ->
                        db.prepareAndUseStatementAsync(PreparedStatementConfig(query.sql)) { statement ->
                            query.params.forEachIndexed { index, paramVal ->
                                statement.setObject(index + 1, paramVal)
                            }
                            statement.executeUpdate()
                        }
                    }
                }

                db.reportRunResultRowDao().getAllByReportUid(request.reportUid)
            }

            emit(
                RunReportUseCase.RunReportResult(
                    timestamp = systemTimeInMillis(),
                    request = request,
                    results = reportQueryResultsToResultStatementReportRows(
                        queryResults = queryResults,
                        request = request
                    ),
                    age = queryResults.age(sinceTimestamp = queries.first().timestamp)
                )
            )
        }
    }
}