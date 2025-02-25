package com.ustadmobile.core.domain.report.query

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.SimpleDoorQuery
import com.ustadmobile.door.ext.dbType
import com.ustadmobile.door.util.systemTimeInMillis

class RunReportUseCaseDatabaseImpl(
    val db: UmAppDatabase,
    val generateReportQueriesUseCase: GenerateReportQueriesUseCase,
) : RunReportUseCase {

    override suspend fun invoke(
        request: RunReportUseCase.RunReportRequest,
    ): RunReportUseCase.RunReportResult {
        val queries = generateReportQueriesUseCase(request.reportOptions, db.dbType())
        return RunReportUseCase.RunReportResult(
            timestamp = systemTimeInMillis(),
            request = request,
            results = queries.map {
                db.statementDao().runReportQuery(SimpleDoorQuery(it.sql, it.params))
            }
        )
    }
}