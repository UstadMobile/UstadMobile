package com.ustadmobile.lib.rest.domain.report.query

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.account.VerifyClientUserSessionUseCase
import com.ustadmobile.core.domain.report.query.RunReportUseCase
import kotlinx.coroutines.flow.first

class RunReportServerUseCase(
    val runReportUseCase: RunReportUseCase,
    private val verifyClientSessionUseCase: VerifyClientUserSessionUseCase,
    val db: UmAppDatabase,
) {

    suspend operator fun invoke(
        request: RunReportUseCase.RunReportRequest,
        fromNodeId: Long,
        nodeAuth: String,
    ): RunReportUseCase.RunReportResult {
        verifyClientSessionUseCase(
            fromNodeId = fromNodeId,
            nodeAuth = nodeAuth,
            accountPersonUid = request.accountPersonUid,
        )

        val report = db.reportDao().findByUid(request.reportUid)
        if(report?.reportOwnerPersonUid != request.accountPersonUid) {
            throw IllegalArgumentException("Request accountPersonUid is not report owner")
        }

        return runReportUseCase(request).first()
    }

}