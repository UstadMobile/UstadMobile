package com.ustadmobile.core.domain.blob.download

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.download.AbstractEnqueueContentManifestDownloadUseCase.Companion.DATA_CONTENTENTRYVERSION_UID
import com.ustadmobile.core.domain.blob.download.AbstractEnqueueContentManifestDownloadUseCase.Companion.DATA_LEARNINGSPACE
import com.ustadmobile.core.domain.blob.download.AbstractEnqueueContentManifestDownloadUseCase.Companion.DATA_JOB_UID
import com.ustadmobile.core.domain.blob.upload.UpdateFailedTransferJobUseCase
import com.ustadmobile.core.util.ext.di
import com.ustadmobile.core.util.ext.isNotCancelled
import com.ustadmobile.core.util.ext.scheduleRetryOrThrow
import com.ustadmobile.door.ext.DoorTag
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.quartz.Job
import org.quartz.JobExecutionContext

class ContentManifestDownloadJob: Job {

    override fun execute(context: JobExecutionContext) {
        val di = context.scheduler.di
        val jobDataMap = context.jobDetail.jobDataMap
        val learningSpace = LearningSpace(jobDataMap.getString(DATA_LEARNINGSPACE))
        val jobUid = jobDataMap.getInt(DATA_JOB_UID)
        val contentEntryVersionUid = jobDataMap.getLong(DATA_CONTENTENTRYVERSION_UID)

        val db: UmAppDatabase = di.on(learningSpace).direct.instance(tag = DoorTag.TAG_DB)
        val contentManifestDownloadUseCase: ContentManifestDownloadUseCase = di.on(learningSpace).
                direct.instance()
        val updateFailedTransferJobUseCase: UpdateFailedTransferJobUseCase by di.on(learningSpace)
            .instance()
        runBlocking {
            try {
                contentManifestDownloadUseCase(
                    contentEntryVersionUid = contentEntryVersionUid,
                    transferJobUid = jobUid
                )
            }catch(e: Throwable) {
                withContext(NonCancellable) {
                    if(db.transferJobDao().isNotCancelled(jobUid)) {
                        try {
                            context.scheduleRetryOrThrow(this@ContentManifestDownloadJob::class.java,
                                ContentManifestDownloadUseCase.DEFAULT_MAX_ATTEMPTS)
                        }catch(e2: Throwable) {
                            updateFailedTransferJobUseCase(jobUid)
                        }
                    }
                }
            }
        }


    }
}