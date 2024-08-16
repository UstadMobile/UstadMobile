package com.ustadmobile.core.domain.blob.upload

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.interruptJobs
import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import org.quartz.Scheduler

class CancelBlobUploadClientUseCaseJvm(
    private val scheduler: Scheduler,
    private val endpoint: Endpoint,
    private val db: UmAppDatabase,
) : CancelBlobUploadClientUseCase{

    override suspend fun invoke(transferJobUid: Int) {
        db.transferJobDao().updateStatus(transferJobUid, TransferJobItemStatus.STATUS_CANCELLED)

        val triggerKey = EnqueueBlobUploadClientUseCaseJvm.triggerKeyFor(
            endpoint, transferJobUid)
        scheduler.unscheduleJob(triggerKey)
        scheduler.interruptJobs(listOf(triggerKey), "cancel blob upload: $transferJobUid")
    }
}