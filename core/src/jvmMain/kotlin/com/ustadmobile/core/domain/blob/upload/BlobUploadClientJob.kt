package com.ustadmobile.core.domain.blob.upload

import com.ustadmobile.core.account.Endpoint
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException

/**
 * Quartz Job to run a blob upload
 */
class BlobUploadClientJob: Job {

    //See https://stackoverflow.com/questions/10893559/refire-quartz-net-trigger-after-15-minutes-if-job-fails-with-exception
    override fun execute(context: JobExecutionContext?) {
        val di = context?.scheduler?.context?.get("di") as DI

        val jobDataMap = context.jobDetail.jobDataMap
        val endpoint = Endpoint(
            jobDataMap.getString(AbstractEnqueueBlobUploadClientUseCase.DATA_ENDPOINT))
        val jobUid = jobDataMap.getInt(AbstractEnqueueBlobUploadClientUseCase.DATA_JOB_UID)
        val blobUploadClientUseCase: BlobUploadClientUseCase = di.on(endpoint).direct.instance()

        runBlocking {
            try {
                blobUploadClientUseCase(jobUid)
            }catch(e: Throwable) {
                val jobException = JobExecutionException(e)
                throw jobException
            }

        }
    }
}