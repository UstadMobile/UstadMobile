package com.ustadmobile.core.contentjob

import org.kodein.di.DI
import org.quartz.Job
import org.quartz.JobExecutionContext

class ContentJobRunnerJob: Job {

    override fun execute(context: JobExecutionContext?) {
        val di = context?.scheduler?.context?.get("di") as DI
        val jobDataMap = context.jobDetail.jobDataMap


    }
}