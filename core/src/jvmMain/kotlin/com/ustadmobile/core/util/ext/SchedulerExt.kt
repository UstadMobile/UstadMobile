package com.ustadmobile.core.util.ext

import com.ustadmobile.core.domain.blob.upload.BlobUploadClientJob
import com.ustadmobile.door.util.systemTimeInMillis
import io.github.aakira.napier.Napier
import org.kodein.di.DI
import org.quartz.Job
import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.JobExecutionContext
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import java.util.Date

private const val RETRY_WAIT_KEY = "com.ustadmobile.wait"

private const val DEFAULT_RETRY_WAIT = 10_000L

/**
 * Set the default retryWait, will be used by any jobs that retry.
 */
var Scheduler.retryWait: Long
    set(value) {
        context.put(RETRY_WAIT_KEY, value)
    }

    get() = if(context.containsKey(RETRY_WAIT_KEY)) {
        context.getLong(RETRY_WAIT_KEY)
    }else {
        DEFAULT_RETRY_WAIT
    }


val Scheduler.di: DI
    get() = context?.get("di") as DI


fun JobExecutionContext.scheduleRetryOrThrow(
    jobClass: Class<out Job>,
    maxAttemptsAllowed: Int,
) {
    val jobDataMap = jobDetail.jobDataMap
    val attemptCount = jobDataMap.getIntOrNull(BlobUploadClientJob.KEY_ATTEMPTS_COUNT) ?: 1
    if(attemptCount < maxAttemptsAllowed) {
        //retry
        val trigger = TriggerBuilder.newTrigger()
            .withIdentity(
                TriggerKey(
                    trigger.key.name + "-retry-$attemptCount",
                    trigger.key.group
                )
            )
            .startAt(Date(systemTimeInMillis() + scheduler.retryWait))
            .build()
        val jobDetail = JobBuilder.newJob(jobClass)
            .usingJobData(mergedJobDataMap)
            .usingJobData(BlobUploadClientJob.KEY_ATTEMPTS_COUNT, attemptCount + 1)
            .build()
        Napier.d("scheduleRetryOrThrow: attempt $attemptCount failed, rescheduling")
        scheduler.scheduleJob(jobDetail, trigger)
    }else {
        throw IllegalStateException("Cannot reschedule: ${trigger.key.name}: attempts exceed $maxAttemptsAllowed")
    }
}

fun Scheduler.unscheduleAnyExistingAndStartNow(
    job: JobDetail,
    triggerKey: TriggerKey
) {
    val jobTrigger = TriggerBuilder.newTrigger()
        .withIdentity(triggerKey)
        .startNow()
        .build()

    Napier.d { "SchedulerExt: scheduleJob with replace $triggerKey" }
    scheduleJob(job, setOf(jobTrigger), true)
    Napier.d { "SchedulerExt: scheduleJob with replace $triggerKey : completed" }
}

