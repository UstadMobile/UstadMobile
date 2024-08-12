package com.ustadmobile.core.domain.contententry.importcontent

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.util.ext.interruptJobs
import org.quartz.Scheduler
import org.quartz.TriggerKey

class CancelImportContentEntryUseCaseJvm(
    private val scheduler: Scheduler,
    private val endpoint: Endpoint,
): CancelImportContentEntryUseCase {

    override fun invoke(cjiUid: Long) {
        val triggerKey = TriggerKey(EnqueueContentEntryImportUseCase.uniqueNameFor(endpoint, cjiUid))
        scheduler.unscheduleJob(triggerKey)
        scheduler.interruptJobs(listOf(triggerKey), "Canceled by user")
    }

}