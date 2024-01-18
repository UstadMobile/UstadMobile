package com.ustadmobile.core.domain.blob.upload

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.util.ext.di
import com.ustadmobile.core.util.ext.scheduleRetryOrThrow
import kotlinx.coroutines.runBlocking
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.quartz.Job
import org.quartz.JobExecutionContext

/**
 * Quartz Job to run a blob upload
 */
class BlobUploadClientJob: Job {

    override fun execute(context: JobExecutionContext) {
        val di = context.scheduler.di
        val jobDataMap = context.jobDetail.jobDataMap
        val endpoint = Endpoint(
            jobDataMap.getString(AbstractEnqueueBlobUploadClientUseCase.DATA_ENDPOINT))
        val blobUploadClientUseCase: BlobUploadClientUseCase = di.on(endpoint).direct.instance()
        val updateFailedTransferJobUseCase: UpdateFailedTransferJobUseCase by di.on(endpoint)
            .instance()

        val jobUid = jobDataMap.getInt(AbstractEnqueueBlobUploadClientUseCase.DATA_JOB_UID)
        runBlocking {
            try {
                blobUploadClientUseCase(jobUid)
            }catch(e: Throwable) {
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

    companion object {

        const val KEY_ATTEMPTS_COUNT = "attempts"

    }

}