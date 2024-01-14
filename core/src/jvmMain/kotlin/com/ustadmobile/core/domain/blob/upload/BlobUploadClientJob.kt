package com.ustadmobile.core.domain.blob.upload

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.util.ext.getIntOrNull
import com.ustadmobile.core.util.ext.retryWait
import com.ustadmobile.door.util.systemTimeInMillis
import io.github.aakira.napier.Napier
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.quartz.Job
import org.quartz.JobBuilder
import org.quartz.JobExecutionContext
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import java.util.Date

/**
 * Quartz Job to run a blob upload
 */
class BlobUploadClientJob: Job {

    override fun execute(context: JobExecutionContext?) {
        val di = context?.scheduler?.context?.get("di") as DI
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
                val attemptCount = jobDataMap.getIntOrNull(KEY_ATTEMPTS_COUNT) ?: 1
                if(attemptCount < BlobUploadClientUseCaseJvm.MAX_ATTEMPTS_DEFAULT) {
                    //retry
                    val trigger = TriggerBuilder.newTrigger()
                        .withIdentity(
                            TriggerKey(
                                context.trigger.key.name + "-retry-$attemptCount",
                                context.trigger.key.group
                            )
                        )
                        .startAt(Date(systemTimeInMillis() + context.scheduler.retryWait))
                        .build()
                    val jobDetail = JobBuilder.newJob(BlobUploadClientJob::class.java)
                        .usingJobData(context.mergedJobDataMap)
                        .usingJobData(KEY_ATTEMPTS_COUNT, attemptCount + 1)
                        .build()
                    Napier.d("BlobUploadClientJob: attempt $attemptCount failed, rescheduling")
                    context.scheduler.scheduleJob(jobDetail, trigger)
                }else {
                    Napier.e("BlobUploadClientJob: FAIL after $attemptCount attempts. No more retries")
                    updateFailedTransferJobUseCase(jobUid)
                }
            }
        }
    }

    companion object {

        const val KEY_ATTEMPTS_COUNT = "attempts"

    }

}