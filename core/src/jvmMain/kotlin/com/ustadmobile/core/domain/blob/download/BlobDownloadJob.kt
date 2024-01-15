package com.ustadmobile.core.domain.blob.download

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.blob.download.AbstractEnqueueBlobDownloadClientUseCase.Companion.DATA_ENDPOINT
import com.ustadmobile.core.domain.blob.download.AbstractEnqueueBlobDownloadClientUseCase.Companion.DATA_JOB_UID
import com.ustadmobile.core.domain.blob.upload.UpdateFailedTransferJobUseCase
import com.ustadmobile.core.util.ext.di
import com.ustadmobile.core.util.ext.scheduleRetryOrThrow
import kotlinx.coroutines.runBlocking
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.quartz.Job
import org.quartz.JobExecutionContext

class BlobDownloadJob : Job{

    override fun execute(context: JobExecutionContext) {
        val di = context.scheduler.di
        val jobDataMap = context.jobDetail.jobDataMap
        val endpoint = Endpoint(jobDataMap.getString(DATA_ENDPOINT))
        val jobUid = jobDataMap.getInt(DATA_JOB_UID)

        val blobDownloadUseCase: BlobDownloadClientUseCase = di.on(endpoint).direct.instance()
        val updateFailedTransferJobUseCase: UpdateFailedTransferJobUseCase by di.on(endpoint)
            .instance()

        runBlocking {
            try {
                blobDownloadUseCase(jobUid)
            }catch(e: Throwable){
                try {
                    context.scheduleRetryOrThrow(
                        BlobDownloadJob::class.java, BlobDownloadClientUseCase.DEFAULT_MAX_ATTEMPTS
                    )
                }catch(e2: Throwable) {
                    updateFailedTransferJobUseCase(jobUid)
                }
            }
        }
    }

}