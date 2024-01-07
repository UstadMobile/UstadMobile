package com.ustadmobile.core.domain.contententry.importcontent

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import org.quartz.Scheduler

class EnqueueImportContentEntryUseCaseJvm(
    private val db: UmAppDatabase,
    private val scheduler: Scheduler,
    private val endpoint: Endpoint,
) : EnqueueContentEntryImportUseCase {

    override suspend fun invoke(
        contentJobItem: ContentEntryImportJob
    ) {
//        val contentJobUid = db.withDoorTransactionAsync {
//            val contentJobId = db.contentJobDao.insertAsync(contentJob)
//            db.contentJobItemDao.insertJobItem(
//                contentJobItem.copy(
//                    cjiJobUid = contentJobId,
//                    cjiLastModified = systemTimeInMillis(),
//                )
//            )
//
//            contentJobId
//        }
//
//
//        val job = JobBuilder.newJob(ContentJobRunnerJob::class.java)
//            .usingJobData(ContentJobManager.KEY_CONTENTJOB_UID, contentJobUid)
//            .usingJobData(ContentJobManager.KEY_ENDPOINT, endpoint.url)
//            .build()
//
//        val triggerKey = TriggerKey("contentjob-${endpoint.url}-$contentJobUid")
//
//        scheduler.unscheduleJob(triggerKey)
//
//        val jobTrigger = TriggerBuilder.newTrigger()
//            .withIdentity(triggerKey)
//            .startNow()
//            .build()
//
//        scheduler.scheduleJob(job, jobTrigger)
//        Napier.d("ImportContentUseCase: scheduled job to import " +
//                "${contentJobItem.sourceUri} #$contentJobUid")
    }
}
