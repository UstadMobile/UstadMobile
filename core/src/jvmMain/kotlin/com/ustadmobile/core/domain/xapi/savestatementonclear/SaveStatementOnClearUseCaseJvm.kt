package com.ustadmobile.core.domain.xapi.savestatementonclear

import com.benasher44.uuid.uuid4
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.xapi.model.XapiStatement
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.quartz.JobBuilder
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey

class SaveStatementOnClearUseCaseJvm(
    private val scheduler: Scheduler,
    private val endpoint: Endpoint,
    private val json: Json,
): SaveStatementOnClearUseCase {

    override fun invoke(
        statements: List<XapiStatement>,
        xapiSession: XapiSessionEntity,
    ) {
        val statementJsonStr = json.encodeToString(
            ListSerializer(XapiStatement.serializer()), statements
        )

        val quartzJob = JobBuilder.newJob(SaveStatementOnClearJob::class.java)
            .usingJobData(SaveStatementOnClearUseCase.KEY_ENDPOINT, endpoint.url)
            .usingJobData(SaveStatementOnClearUseCase.KEY_STATEMENTS, statementJsonStr)
            .usingJobData(SaveStatementOnClearUseCase.KEY_XAPI_SESSION,
                json.encodeToString(XapiSessionEntity.serializer(), xapiSession)
            )
            .build()

        val triggerKey = TriggerKey("save-statement-${uuid4()}")
        val jobTrigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .startNow()
            .build()
        scheduler.scheduleJob(quartzJob, jobTrigger)
    }
}