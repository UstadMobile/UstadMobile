package com.ustadmobile.core.domain.report.query

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.report.query.RunReportUseCase.Companion.reportQueryResultsToResultStatementReportRows
import com.ustadmobile.core.util.ext.age
import com.ustadmobile.core.util.ext.bodyAsDecodedText
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.ext.doorNodeIdHeader
import com.ustadmobile.door.ext.setBodyJson
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ReportQueryResult
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.http.appendPathSegments
import io.ktor.http.takeFrom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

/**
 * Client side implementation of RunReportUseCase. The flow will return cached results from the
 * local database immediately when available. If cached results are not available, or no longer
 * fresh, then a request will be made to the server. Those updated results will be cached and then
 * emitted from the flow.
 */
class RunReportUseCaseClientImpl(
    private val db: UmAppDatabase,
    private val clientNodeId: Long,
    private val clientNodeAuth: String,
    private val learningSpace: Endpoint,
    private val httpClient: HttpClient,
    private val json: Json,
): RunReportUseCase {

    constructor(
        db: UmAppDatabase,
        repo: DoorDatabaseRepository,
        learningSpace: Endpoint,
        httpClient: HttpClient,
        json: Json,
    ): this(db, repo.config.nodeId, repo.config.auth, learningSpace, httpClient, json)

    override fun invoke(
        request: RunReportUseCase.RunReportRequest
    ): Flow<RunReportUseCase.RunReportResult> {
        return flow {
            val currentReportQueryResults = db.reportRunResultRowDao().getAllByReportUidAndTimeZone(
                request.reportUid, request.timeZone.id
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
                freshThresholdTime = systemTimeInMillis() - (request.maxFreshAge * 1000),
                timeZone = request.timeZone.id
            )

            if(!isFresh) {
                val rowsJsonText = try {
                    httpClient.post {
                        url {
                            takeFrom(learningSpace.url)
                            appendPathSegments("api/report/run")
                        }

                        doorNodeIdHeader(clientNodeId, clientNodeAuth)
                        setBodyJson(json, RunReportUseCase.RunReportRequest.serializer(), request)
                    }.bodyAsDecodedText()
                }catch(e: Throwable) {
                    e.printStackTrace()
                    throw e
                }

                val response = json.decodeFromString(
                    RunReportUseCase.RunReportResult.serializer(), rowsJsonText
                )
                val lastModTime = systemTimeInMillis() - (response.age * 1000)

                val responseQueryResults = response.results.flatMapIndexed { index, rows ->
                    val requestSeries = request.reportOptions.series[index]

                    rows.map {
                        ReportQueryResult(
                            rqrReportUid = request.reportUid,
                            rqrLastModified = lastModTime,
                            rqrLastValidated = systemTimeInMillis(),
                            rqrReportSeriesUid = requestSeries.reportSeriesUid,
                            rqrYAxis = it.yAxis,
                            rqrXAxis = it.xAxis,
                            rqrSubgroup = it.subgroup,
                        )
                    }
                }

                db.withDoorTransactionAsync {
                    db.reportRunResultRowDao().deleteByReportUidAndTimeZone(
                        request.reportUid, request.timeZone.id
                    )
                    db.reportRunResultRowDao().insertAllAsync(responseQueryResults)
                }

                emit(response)
            }
        }
    }
}