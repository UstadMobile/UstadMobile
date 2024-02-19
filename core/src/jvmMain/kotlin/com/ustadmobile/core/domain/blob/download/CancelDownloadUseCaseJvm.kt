package com.ustadmobile.core.domain.blob.download

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.connectivitymonitor.ConnectivityTriggerGroupController
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import io.github.aakira.napier.Napier
import org.quartz.Scheduler
import org.quartz.TriggerKey

class CancelDownloadUseCaseJvm(
    private val scheduler: Scheduler,
    private val endpoint: Endpoint,
    private val db: UmAppDatabase,
) : CancelDownloadUseCase{

    override suspend fun invoke(transferJobId: Int, offlineItemUid: Long) {
        //mark as canceled immediately so any interrupted job knows not to attempt a retry
        db.transferJobDao.updateStatus(transferJobId, TransferJobItemStatus.STATUS_CANCELLED)

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

        val jobsToCancel = scheduler.currentlyExecutingJobs.filter {
            it.trigger.key in triggerKeys
        }

        jobsToCancel.forEach {
            val triggerKey = it.trigger.key
            try {
                scheduler.interrupt(it.fireInstanceId)
                Napier.d { "CancelDownloadUseCaseJvm: interrupted $triggerKey $transferJobId/$offlineItemUid" }
            }catch(e: Throwable) {
                Napier.w { "CancelDownloadUseCase: Exception attempting to interrupt $triggerKey" +
                        " $transferJobId/$offlineItemUid" }
            }
        }

        db.offlineItemDao.updateActiveByOfflineItemUid(offlineItemUid, false)
    }
}