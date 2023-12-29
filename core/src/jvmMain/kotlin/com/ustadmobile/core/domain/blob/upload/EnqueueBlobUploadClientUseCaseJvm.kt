package com.ustadmobile.core.domain.blob.upload

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.libcache.UstadCache
import org.quartz.JobBuilder
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import org.quartz.impl.matchers.GroupMatcher

/**
 * Implementation of EnqueueBlobUploadClient using Quartz as the scheduler.
 */
class EnqueueBlobUploadClientUseCaseJvm(
    private val scheduler: Scheduler,
    private val endpoint: Endpoint,
    db: UmAppDatabase,
    cache: UstadCache,
): AbstractEnqueueBlobUploadClientUseCase(
    db = db, cache = cache
) {

    //See http://www.quartz-scheduler.org/documentation/quartz-2.3.0/cookbook/StoreJob.html
    override suspend fun invoke(
        items: List<EnqueueBlobUploadClientUseCase.EnqueueBlobUploadItem>,
        batchUuid: String,
        chunkSize: Int
    ) {
        val transferJob = createTransferJob(items, batchUuid)
        val jobKey = JobKey("foo")
        val quartzJob = JobBuilder.newJob(BlobUploadClientJob::class.java)
            .withIdentity(jobKey)
            .usingJobData(DATA_JOB_UID, transferJob.tjUid)
            .usingJobData(DATA_ENDPOINT, endpoint.url)
            .build()

        scheduler.getJobDetail(jobKey)
        scheduler.getJobKeys(GroupMatcher.groupEquals("pending-connectivity"))
        val triggerKey = TriggerKey("blob-upload-${endpoint.url}-${transferJob.tjUid}")
        scheduler.unscheduleJob(triggerKey)
        val jobTrigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .startNow()
            .build()

        scheduler.scheduleJob(quartzJob, jobTrigger)
    }
}