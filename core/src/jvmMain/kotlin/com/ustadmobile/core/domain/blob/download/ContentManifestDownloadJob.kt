package com.ustadmobile.core.domain.blob.download

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.blob.download.AbstractEnqueueContentManifestDownloadUseCase.Companion.DATA_CONTENTENTRYVERSION_UID
import com.ustadmobile.core.domain.blob.download.AbstractEnqueueContentManifestDownloadUseCase.Companion.DATA_ENDPOINT
import com.ustadmobile.core.domain.blob.download.AbstractEnqueueContentManifestDownloadUseCase.Companion.DATA_JOB_UID
import com.ustadmobile.core.domain.blob.upload.UpdateFailedTransferJobUseCase
import com.ustadmobile.core.util.ext.di
import com.ustadmobile.core.util.ext.scheduleRetryOrThrow
import kotlinx.coroutines.runBlocking
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.quartz.Job
import org.quartz.JobExecutionContext

class ContentManifestDownloadJob: Job {

    override fun execute(context: JobExecutionContext) {
        val di = context.scheduler.di
        val jobDataMap = context.jobDetail.jobDataMap
        val endpoint = Endpoint(jobDataMap.getString(DATA_ENDPOINT))
        val jobUid = jobDataMap.getInt(DATA_JOB_UID)
        val contentEntryVersionUid = jobDataMap.getLong(DATA_CONTENTENTRYVERSION_UID)

        val contentManifestDownloadUseCase: ContentManifestDownloadUseCase = di.on(endpoint).
                direct.instance()
        val updateFailedTransferJobUseCase: UpdateFailedTransferJobUseCase by di.on(endpoint)
            .instance()
        runBlocking {
            try {
                contentManifestDownloadUseCase(
                    contentEntryVersionUid = contentEntryVersionUid,
                    transferJobUid = jobUid
                )
            }catch(e: Throwable) {
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