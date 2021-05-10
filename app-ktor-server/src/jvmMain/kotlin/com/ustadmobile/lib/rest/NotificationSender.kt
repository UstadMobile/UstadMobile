package com.ustadmobile.lib.rest

import com.ustadmobile.lib.rest.messaging.SendEmailJob
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.direct
import org.kodein.di.instance
import org.quartz.JobBuilder.newJob
import org.quartz.Scheduler
import org.quartz.TriggerBuilder

class NotificationSender(override val di: DI) : DIAware {

    fun sendEmail(toAddr: String, subject: String, message: String){
        val scheduler: Scheduler = di.direct.instance()

        val job = newJob(SendEmailJob::class.java)
            .usingJobData(SendEmailJob.INPUT_MESSAGE, message)
            .usingJobData(SendEmailJob.INPUT_SUBJECT, subject)
            .usingJobData(SendEmailJob.INPUT_TO, toAddr)
            .build()

        val jobTrigger = TriggerBuilder.newTrigger()
            .startNow()
            .build()

        scheduler.scheduleJob(job, jobTrigger)
    }

}