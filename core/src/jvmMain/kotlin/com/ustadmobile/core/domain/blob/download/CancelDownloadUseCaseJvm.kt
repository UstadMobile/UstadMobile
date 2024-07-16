package com.ustadmobile.core.domain.blob.download

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.connectivitymonitor.ConnectivityTriggerGroupController
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.interruptJobs
import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import org.quartz.Scheduler
import org.quartz.TriggerKey

class CancelDownloadUseCaseJvm(
    private val scheduler: Scheduler,
    private val endpoint: Endpoint,
    private val db: UmAppDatabase,
) : CancelDownloadUseCase{

    override suspend fun invoke(transferJobId: Int, offlineItemUid: Long) {
        //mark as canceled immediately so any interrupted job knows not to attempt a retry
        db.transferJobDao().updateStatus(transferJobId, TransferJobItemStatus.STATUS_CANCELLED)

        //uneschedule
        val triggerKeys = listOf(
            TriggerKey.triggerKey(
                AbstractEnqueueContentManifestDownloadUseCase.uniqueNameFor(endpoint, transferJobId),
                ConnectivityTriggerGroupController.TRIGGERKEY_CONNECTIVITY_REQUIRED_GROUP
            ),
            TriggerKey.triggerKey(
                AbstractEnqueueBlobDownloadClientUseCase.uniqueNameFor(endpoint, transferJobId),
                ConnectivityTriggerGroupController.TRIGGERKEY_CONNECTIVITY_REQUIRED_GROUP
            )
        )
        scheduler.unscheduleJobs(triggerKeys)
        scheduler.interruptJobs(triggerKeys, "download cancel: $transferJobId/$offlineItemUid")


        db.offlineItemDao().updateActiveByOfflineItemUid(offlineItemUid, false)
    }
}