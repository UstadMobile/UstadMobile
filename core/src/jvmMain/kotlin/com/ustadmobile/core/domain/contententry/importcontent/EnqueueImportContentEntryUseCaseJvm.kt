package com.ustadmobile.core.domain.contententry.importcontent

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.isRemote
import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import io.github.aakira.napier.Napier
import org.quartz.JobBuilder
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey

/**
 * @param enqueueRemoteImport optional delegate that can be used to send a remote (eg. http/https)
 *        content entry import job to the server for processing. If this use case is running on the
 *        Desktop version, this will be non-null and remote jobs will be sent to server. If this is
 *        being used on the http server itself, this will be null
 */
class EnqueueImportContentEntryUseCaseJvm(
    private val db: UmAppDatabase,
    private val scheduler: Scheduler,
    private val learningSpace: LearningSpace,
    private val enqueueRemoteImport: EnqueueContentEntryImportUseCase?,
) : EnqueueContentEntryImportUseCase {

    override suspend fun invoke(
        contentJobItem: ContentEntryImportJob
    ) {
        val jobUri = DoorUri.parse(contentJobItem.sourceUri!!)
        val enqueueRemoteImportVal = enqueueRemoteImport
        if(enqueueRemoteImportVal != null && jobUri.isRemote()) {
            enqueueRemoteImportVal(contentJobItem)
        }else {
            val uid = db.contentEntryImportJobDao().insertJobItem(contentJobItem)
            val quartzJob = JobBuilder.newJob(ImportContentEntryJob::class.java)
                .usingJobData(EnqueueContentEntryImportUseCase.DATA_LEARNINGSPACE, learningSpace.url)
                .usingJobData(EnqueueContentEntryImportUseCase.DATA_JOB_UID, uid)
                .build()
            val triggerKey = TriggerKey(EnqueueContentEntryImportUseCase.uniqueNameFor(learningSpace, uid))
            scheduler.unscheduleJob(triggerKey)
            val jobTrigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .startNow()
                .build()
            scheduler.scheduleJob(quartzJob, jobTrigger)

            Napier.d("ImportContentUseCase: scheduled job to import " +
                    "${contentJobItem.sourceUri} #$uid")
        }
    }
}
