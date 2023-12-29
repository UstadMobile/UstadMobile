package com.ustadmobile.core.domain.blob.upload

import com.ustadmobile.core.account.Endpoint
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.quartz.Job
import org.quartz.JobExecutionContext

/**
 * Quartz Job to run a blob upload
 */
class BlobUploadClientJob: Job {

    override fun execute(context: JobExecutionContext?) {
        val di = context?.scheduler?.context?.get("di") as DI
        val jobDataMap = context.jobDetail.jobDataMap
        val endpoint = Endpoint(
            jobDataMap.getString(AbstractEnqueueBlobUploadClientUseCase.DATA_ENDPOINT))
        val jobUid = jobDataMap.getInt(AbstractEnqueueBlobUploadClientUseCase.DATA_JOB_UID)
        val blobUploadClientUseCase: BlobUploadClientUseCase = di.on(endpoint).direct.instance()

        runBlocking {
            blobUploadClientUseCase(jobUid)
        }
    }
}