package com.ustadmobile.core.domain.contententry.importcontent

import com.ustadmobile.core.account.Endpoint
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.quartz.Job
import org.quartz.JobExecutionContext

class ImportContentEntryJob : Job{
    override fun execute(context: JobExecutionContext?) {
        val di = context?.scheduler?.context?.get("di") as DI
        val jobDataMap = context.jobDetail.jobDataMap
        val endpoint = Endpoint(jobDataMap.getString(EnqueueContentEntryImportUseCase.DATA_ENDPOINT))
        val uid = jobDataMap.getLong(EnqueueContentEntryImportUseCase.DATA_JOB_UID)
        val importUseCase: ImportContentEntryUseCase = di.on(endpoint).direct.instance()
        runBlocking {
            importUseCase(uid)
        }
    }
}