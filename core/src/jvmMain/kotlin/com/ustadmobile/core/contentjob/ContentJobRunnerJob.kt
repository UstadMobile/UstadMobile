package com.ustadmobile.core.contentjob

import com.ustadmobile.core.account.Endpoint
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.quartz.Job
import org.quartz.JobExecutionContext

class ContentJobRunnerJob: Job {

    override fun execute(context: JobExecutionContext?) {
        val di = context?.scheduler?.context?.get("di") as DI
        val jobDataMap = context.jobDetail.jobDataMap

        val jobId = jobDataMap.getLong(ContentJobManager.KEY_CONTENTJOB_UID)
        val endpoint = Endpoint(jobDataMap.getString(ContentJobManager.KEY_ENDPOINT))

        runBlocking {
            ContentImportJobRunner(jobId, endpoint, di).runJob()
        }

    }
}