package com.ustadmobile.core.domain.report.query

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.report.query.RunReportUseCase.Companion.reportQueryResultsToResultStatementReportRows
import com.ustadmobile.core.util.ext.age
import com.ustadmobile.door.util.systemTimeInMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RunReportUseCaseClientImpl(
    private val db: UmAppDatabase,
    private val learningSpace: Endpoint,
): RunReportUseCase {

    override fun invoke(
        request: RunReportUseCase.RunReportRequest
    ): Flow<RunReportUseCase.RunReportResult> {
        return flow {
            val currentReportQueryResults = db.reportRunResultRowDao().getAllByReportUid(
                request.reportUid
            )

            emit(
                RunReportUseCase.RunReportResult(
                    timestamp = systemTimeInMillis(),
                    request = request,
                    results = reportQueryResultsToResultStatementReportRows(
                        queryResults = currentReportQueryResults,
                        request = request
                    ),
                    age = currentReportQueryResults.age(sinceTimestamp = systemTimeInMillis()),
                )
            )

            val isFresh = db.reportRunResultRowDao().isReportFresh(
                reportUid = request.reportUid,
                freshThresholdTime = systemTimeInMillis() - (request.maxFreshAge * 1000)
            )

        }
    }
}