package com.ustadmobile.core.domain.contententry.importcontent

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import io.github.aakira.napier.Napier
import org.quartz.JobBuilder
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey

class EnqueueImportContentEntryUseCaseJvm(
    private val db: UmAppDatabase,
    private val scheduler: Scheduler,
    private val endpoint: Endpoint,
) : EnqueueContentEntryImportUseCase {

    override suspend fun invoke(
        contentJobItem: ContentEntryImportJob
    ) {
        val uid = db.contentEntryImportJobDao.insertJobItem(contentJobItem)
        val quartzJob = JobBuilder.newJob(ImportContentEntryJob::class.java)
            .usingJobData(EnqueueContentEntryImportUseCase.DATA_ENDPOINT, endpoint.url)
            .usingJobData(EnqueueContentEntryImportUseCase.DATA_JOB_UID, uid)
            .build()
        val triggerKey = TriggerKey("contententryimport-${endpoint.url}-$uid")
        scheduler.unscheduleJob(triggerKey)
        val jobTrigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .startNow()
            .build()
        scheduler.scheduleJob(quartzJob, jobTrigger)

        Napier.d("ImportContentUseCase: scheduled job to import " +
                "${contentJobItem.sourceUri} #$uid")
    }
}
