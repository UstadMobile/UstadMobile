package com.ustadmobile.core.domain.blob.upload

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.InterruptableCoroutineJob
import com.ustadmobile.core.util.ext.di
import com.ustadmobile.core.util.ext.isNotCancelled
import com.ustadmobile.core.util.ext.scheduleRetryOrThrow
import com.ustadmobile.door.ext.DoorTag
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.quartz.JobExecutionContext

/**
 * Quartz Job to run a blob upload
 */
class BlobUploadClientJob: InterruptableCoroutineJob() {

    override suspend fun executeAsync(context: JobExecutionContext) {
        val di = context.scheduler.di
        val jobDataMap = context.jobDetail.jobDataMap
        val endpoint = LearningSpace(
            jobDataMap.getString(AbstractEnqueueBlobUploadClientUseCase.DATA_LEARNINGSPACE))
        val blobUploadClientUseCase: BlobUploadClientUseCase = di.on(endpoint).direct.instance()
        val updateFailedTransferJobUseCase: UpdateFailedTransferJobUseCase by di.on(endpoint)
            .instance()
        val db: UmAppDatabase = di.on(endpoint).direct.instance(tag = DoorTag.TAG_DB)

        val jobUid = jobDataMap.getInt(AbstractEnqueueBlobUploadClientUseCase.DATA_JOB_UID)

        try {
            blobUploadClientUseCase(jobUid)
        }catch(e: Throwable) {
            withContext(NonCancellable) {
                if (db.transferJobDao().isNotCancelled(jobUid)) {
                    try {
                        context.scheduleRetryOrThrow(
                            BlobUploadClientJob::class.java, BlobUploadClientUseCaseJvm.MAX_ATTEMPTS_DEFAULT
                        )
                    }catch(e2: Throwable) {
                        updateFailedTransferJobUseCase(jobUid)
                    }
                }
            }
        }
    }

    companion object {

        const val KEY_ATTEMPTS_COUNT = "attempts"

    }

}