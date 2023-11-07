package com.ustadmobile.core.domain.contententry.import

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.ContentJobManager
import com.ustadmobile.core.contentjob.ContentJobRunnerJob
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentJobItem
import io.github.aakira.napier.Napier
import org.quartz.JobBuilder
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey

class ImportContentUseCaseJvm(
    private val db: UmAppDatabase,
    private val scheduler: Scheduler,
    private val endpoint: Endpoint,
) : ImportContentUseCase{

    override suspend fun invoke(
        contentJob: ContentJob,
        contentJobItem: ContentJobItem
    ) {
        val contentJobUid = db.withDoorTransactionAsync {
            val contentJobId = db.contentJobDao.insertAsync(contentJob)
            db.contentJobItemDao.insertJobItem(
                contentJobItem.copy(
                    cjiJobUid = contentJobId,
                    cjiLastModified = systemTimeInMillis(),
                )
            )

            contentJobId
        }


        val job = JobBuilder.newJob(ContentJobRunnerJob::class.java)
            .usingJobData(ContentJobManager.KEY_CONTENTJOB_UID, contentJobUid)
            .usingJobData(ContentJobManager.KEY_ENDPOINT, endpoint.url)
            .build()

        val triggerKey = TriggerKey("contentjob-${endpoint.url}-$contentJobUid")

        scheduler.unscheduleJob(triggerKey)

        val jobTrigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .startNow()
            .build()

        scheduler.scheduleJob(job, jobTrigger)
        Napier.d("ImportContentUseCase: scheduled job to import " +
                "${contentJobItem.sourceUri} #$contentJobUid")
    }
}
