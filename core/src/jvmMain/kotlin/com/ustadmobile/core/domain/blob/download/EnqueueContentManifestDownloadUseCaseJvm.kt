package com.ustadmobile.core.domain.blob.download

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.connectivitymonitor.ConnectivityTriggerGroupController
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.unscheduleAnyExistingAndStartNow
import org.quartz.JobBuilder
import org.quartz.Scheduler
import org.quartz.TriggerKey

class EnqueueContentManifestDownloadUseCaseJvm(
    private val scheduler: Scheduler,
    private val learningSpace: LearningSpace,
    db: UmAppDatabase,
) : AbstractEnqueueContentManifestDownloadUseCase(db){

    override suspend fun invoke(
        contentEntryVersionUid: Long,
        offlineItemUid: Long,
    ) {
        val transferJob = createTransferJob(
            contentEntryVersionUid = contentEntryVersionUid,
            offlineItemUid = offlineItemUid
        )
        val quartzJob = JobBuilder.newJob(ContentManifestDownloadJob::class.java)
            .usingJobData(DATA_LEARNINGSPACE, learningSpace.url)
            .usingJobData(DATA_JOB_UID, transferJob.tjUid)
            .usingJobData(DATA_CONTENTENTRYVERSION_UID, contentEntryVersionUid)
            .build()

        val triggerKey = TriggerKey(
            uniqueNameFor(learningSpace, transferJob.tjUid),
            ConnectivityTriggerGroupController.TRIGGERKEY_CONNECTIVITY_REQUIRED_GROUP)

        scheduler.unscheduleAnyExistingAndStartNow(quartzJob, triggerKey)
    }
}