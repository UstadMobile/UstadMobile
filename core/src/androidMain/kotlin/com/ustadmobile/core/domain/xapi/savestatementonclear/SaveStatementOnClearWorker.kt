package com.ustadmobile.core.domain.xapi.savestatementonclear

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.core.domain.xapi.XapiStatementResource
import com.ustadmobile.core.domain.xapi.model.XapiStatement
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class SaveStatementOnClearWorker(
    context: Context,
    params: WorkerParameters
): CoroutineWorker(context, params) {

    private val di: DI by closestDI { applicationContext }

    override suspend fun doWork(): Result {
        val endpointUrl = inputData.getString(SaveStatementOnClearUseCase.KEY_ENDPOINT)
            ?: throw IllegalArgumentException("no endpoint")
        val statementsStr = inputData.getString(SaveStatementOnClearUseCase.KEY_STATEMENTS)
            ?: throw IllegalArgumentException("no statements")
        val xapiSessionStr = inputData.getString(SaveStatementOnClearUseCase.KEY_XAPI_SESSION)
            ?: throw IllegalArgumentException("no xapisession")

        val json: Json = di.direct.instance()
        val statementResource: XapiStatementResource = di.on(Endpoint(endpointUrl))
            .direct.instance()

        statementResource.post(
            statements = json.decodeFromString(ListSerializer(XapiStatement.serializer()), statementsStr),
            xapiSession = json.decodeFromString(XapiSession.serializer(), xapiSessionStr),
        )

        return Result.success()
    }
}