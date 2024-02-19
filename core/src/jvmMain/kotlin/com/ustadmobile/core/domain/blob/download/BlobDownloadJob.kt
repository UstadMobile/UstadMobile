package com.ustadmobile.core.domain.blob.download

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.download.AbstractEnqueueBlobDownloadClientUseCase.Companion.DATA_ENDPOINT
import com.ustadmobile.core.domain.blob.download.AbstractEnqueueBlobDownloadClientUseCase.Companion.DATA_JOB_UID
import com.ustadmobile.core.domain.blob.upload.UpdateFailedTransferJobUseCase
import com.ustadmobile.core.util.ext.di
import com.ustadmobile.core.util.ext.isNotCancelled
import com.ustadmobile.core.util.ext.scheduleRetryOrThrow
import com.ustadmobile.door.ext.DoorTag
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.quartz.InterruptableJob
import org.quartz.JobExecutionContext
import kotlinx.coroutines.Job as CoroutineJob

class BlobDownloadJob : InterruptableJob{

    private val scope = CoroutineScope(Dispatchers.IO + CoroutineJob())

    override fun execute(context: JobExecutionContext) {
        val di = context.scheduler.di
        val jobDataMap = context.jobDetail.jobDataMap
        val endpoint = Endpoint(jobDataMap.getString(DATA_ENDPOINT))
        val jobUid = jobDataMap.getInt(DATA_JOB_UID)
        val logPrefix = "BlobDownloadJob: #$jobUid:"

        val blobDownloadUseCase: BlobDownloadClientUseCase = di.on(endpoint).direct.instance()
        val updateFailedTransferJobUseCase: UpdateFailedTransferJobUseCase by di.on(endpoint)
            .instance()
        val db: UmAppDatabase = di.on(endpoint).direct.instance(tag = DoorTag.TAG_DB)

        runBlocking {
            withContext(scope.coroutineContext) {
                try {
                    Napier.d("$logPrefix : starting: invoke use case")
                    blobDownloadUseCase(jobUid)
                }catch(e: Throwable){
                    Napier.w("$logPrefix : attempt exception", e)
                    withContext(NonCancellable) {
                        if(db.transferJobDao.isNotCancelled(jobUid)) {
                            try {
                                context.scheduleRetryOrThrow(
                                    BlobDownloadJob::class.java, BlobDownloadClientUseCase.DEFAULT_MAX_ATTEMPTS
                                )
                            }catch(e2: Throwable) {
                                updateFailedTransferJobUseCase(jobUid)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun interrupt() {
        scope.cancel("Job Interrupted")
    }
}