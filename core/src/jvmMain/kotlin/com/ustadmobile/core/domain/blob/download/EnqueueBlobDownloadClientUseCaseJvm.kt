package com.ustadmobile.core.domain.blob.download

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.connectivitymonitor.ConnectivityTriggerGroupController
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.unscheduleAnyExistingAndStartNow
import io.github.aakira.napier.Napier
import org.quartz.JobBuilder
import org.quartz.Scheduler
import org.quartz.TriggerKey

class EnqueueBlobDownloadClientUseCaseJvm(
    private val scheduler: Scheduler,
    private val learningSpace: LearningSpace,
    db: UmAppDatabase,
): AbstractEnqueueBlobDownloadClientUseCase(db) {
    override suspend fun invoke(
        items: List<EnqueueBlobDownloadClientUseCase.EnqueueBlobDownloadItem>,
        existingTransferJobId: Int
    ) {
        val transferJob = createTransferJob(items, existingTransferJobId)
        val uniqueName = uniqueNameFor(learningSpace, transferJob.tjUid)
        val quartzJob = JobBuilder.newJob(BlobDownloadJob::class.java)
            .usingJobData(DATA_LEARNINGSPACE, learningSpace.url)
            .usingJobData(DATA_JOB_UID, transferJob.tjUid)
            .build()
        val triggerKey = TriggerKey(
            uniqueName,
            ConnectivityTriggerGroupController.TRIGGERKEY_CONNECTIVITY_REQUIRED_GROUP
        )

        Napier.d { "EnqueueBlobDownloadClientUseCaseJvm: scheduled job via quartz. JobId=${transferJob.tjUid} " }
        scheduler.unscheduleAnyExistingAndStartNow(quartzJob, triggerKey)
    }

}