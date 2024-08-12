package com.ustadmobile.core.domain.xapi.savestatementonclear

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.blob.InterruptableCoroutineJob
import com.ustadmobile.core.domain.xapi.XapiStatementResource
import com.ustadmobile.core.domain.xapi.model.XapiStatement
import com.ustadmobile.core.util.ext.di
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity
import io.github.aakira.napier.Napier
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.kodein.di.instance
import org.kodein.di.on
import org.quartz.JobExecutionContext

class SaveStatementOnClearJob: InterruptableCoroutineJob() {

    override suspend fun executeAsync(context: JobExecutionContext) {
        val di = context.scheduler.di
        val jobDataMap = context.jobDetail.jobDataMap
        val endpoint = Endpoint(jobDataMap.getString(SaveStatementOnClearUseCase.KEY_ENDPOINT))
        val statementResource: XapiStatementResource by di.on(endpoint).instance()
        val json: Json by di.instance()

        try {
            val statements = json.decodeFromString(
                ListSerializer(XapiStatement.serializer()),
                jobDataMap.getString(SaveStatementOnClearUseCase.KEY_STATEMENTS)
            )
            val xapiSession = json.decodeFromString(
                XapiSessionEntity.serializer(),
                jobDataMap.getString(SaveStatementOnClearUseCase.KEY_XAPI_SESSION)
            )

            statementResource.post(statements, xapiSession)
        }catch(e: Throwable) {
            Napier.e(throwable = e) { "SaveStatementOnClear: exception" }
        }
    }

}