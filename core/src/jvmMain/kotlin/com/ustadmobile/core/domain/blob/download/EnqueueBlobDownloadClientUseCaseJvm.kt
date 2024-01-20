package com.ustadmobile.core.domain.blob.download

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.connectivitymonitor.ConnectivityTriggerGroupController
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.unscheduleAnyExistingAndStartNow
import io.github.aakira.napier.Napier
import org.quartz.JobBuilder
import org.quartz.Scheduler
import org.quartz.TriggerKey

class EnqueueBlobDownloadClientUseCaseJvm(
    private val scheduler: Scheduler,
    private val endpoint: Endpoint,
    db: UmAppDatabase,
): AbstractEnqueueBlobDownloadClientUseCase(db) {
    override suspend fun invoke(
        items: List<EnqueueBlobDownloadClientUseCase.EnqueueBlobDownloadItem>,
        existingTransferJobId: Int
    ) {
        val transferJob = createTransferJob(items, existingTransferJobId)
        val quartzJob = JobBuilder.newJob(BlobDownloadJob::class.java)
            .usingJobData(DATA_ENDPOINT, endpoint.url)
            .usingJobData(DATA_JOB_UID, transferJob.tjUid)
            .build()
        val triggerKey = TriggerKey("blob-download-${endpoint.url}-${transferJob.tjUid}",
            ConnectivityTriggerGroupController.TRIGGERKEY_CONNECTIVITY_REQUIRED_GROUP)

        Napier.d { "EnqueueBlobDownloadClientUseCaseJvm: scheduled job via quartz. JobId=${transferJob.tjUid} " }
        scheduler.unscheduleAnyExistingAndStartNow(quartzJob, triggerKey)
    }

}