package com.ustadmobile.core.domain.blob.upload

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.connectivitymonitor.ConnectivityTriggerGroupController
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.TransferJob
import com.ustadmobile.libcache.UstadCache
import io.github.aakira.napier.Napier
import org.quartz.JobBuilder
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey

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
        chunkSize: Int,
        tableId: Int,
        entityUid: Long,
    ): TransferJob {
        val transferJob = createTransferJob(items, batchUuid, tableId, entityUid)
        val quartzJob = JobBuilder.newJob(BlobUploadClientJob::class.java)
            .usingJobData(DATA_JOB_UID, transferJob.tjUid)
            .usingJobData(DATA_ENDPOINT, endpoint.url)
            .build()

        val triggerKey = TriggerKey(
            "blob-upload-${endpoint.url}-${transferJob.tjUid}",
            ConnectivityTriggerGroupController.TRIGGERKEY_CONNECTIVITY_REQUIRED_GROUP
        )
        scheduler.unscheduleJob(triggerKey)
        val jobTrigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .startNow()
            .build()

        Napier.d("EnqueueBlobUploadClientUseCase($batchUuid): scheduled job " +
                "#${transferJob.tjUid} via quartz")
        scheduler.scheduleJob(quartzJob, jobTrigger)

        return transferJob
    }
}