package com.ustadmobile.core.contentjob

import com.ustadmobile.core.account.Endpoint
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.direct
import org.kodein.di.instance
import org.quartz.JobBuilder
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey

class ContentJobManagerJvm(override val di: DI): ContentJobManager, DIAware {

    override fun enqueueContentJob(endpoint: Endpoint, contentJobUid: Long) {
        val scheduler: Scheduler = di.direct.instance()

        val job = JobBuilder.newJob(ContentJobRunnerJob::class.java)
            .usingJobData(ContentJobManager.KEY_CONTENTJOB_UID, contentJobUid)
            .usingJobData(ContentJobManager.KEY_ENDPOINT, endpoint.url)
            .build()

        val triggerKey = TriggerKey("contentjob-${endpoint.url}-$contentJobUid")

        scheduler.unscheduleJob(triggerKey)

        val jobTrigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .startNow()
            .build()

        scheduler.scheduleJob(job, jobTrigger)

    }

    override fun cancelContentJob(endpoint: Endpoint, contentJobUid: Long) {
        val scheduler: Scheduler = di.direct.instance()
        val triggerKey = TriggerKey("contentjob-${endpoint.url}-$contentJobUid")
        scheduler.unscheduleJob(triggerKey)
    }

}