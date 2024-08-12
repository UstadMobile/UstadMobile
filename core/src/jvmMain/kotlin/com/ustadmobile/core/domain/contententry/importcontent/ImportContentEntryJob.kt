package com.ustadmobile.core.domain.contententry.importcontent

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.blob.InterruptableCoroutineJob
import com.ustadmobile.core.util.ext.di
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.quartz.JobExecutionContext

class ImportContentEntryJob : InterruptableCoroutineJob() {

    override suspend fun executeAsync(context: JobExecutionContext)  {
        val di = context.scheduler.di
        val jobDataMap = context.jobDetail.jobDataMap
        val endpoint = Endpoint(jobDataMap.getString(EnqueueContentEntryImportUseCase.DATA_ENDPOINT))
        val uid = jobDataMap.getLong(EnqueueContentEntryImportUseCase.DATA_JOB_UID)
        val importUseCase: ImportContentEntryUseCase = di.on(endpoint).direct.instance()
        importUseCase(uid)
    }
}