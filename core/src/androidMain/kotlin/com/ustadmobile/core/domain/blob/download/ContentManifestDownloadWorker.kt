package com.ustadmobile.core.domain.blob.download

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.blob.download.AbstractEnqueueContentManifestDownloadUseCase.Companion.DATA_CONTENTENTRYVERSION_UID
import com.ustadmobile.core.domain.blob.download.AbstractEnqueueContentManifestDownloadUseCase.Companion.DATA_ENDPOINT
import com.ustadmobile.core.domain.blob.download.AbstractEnqueueContentManifestDownloadUseCase.Companion.DATA_JOB_UID
import com.ustadmobile.core.domain.blob.upload.UpdateFailedTransferJobUseCase
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class ContentManifestDownloadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params){

    private val di: DI by closestDI { applicationContext }

    override suspend fun doWork(): Result {
        val endpointUrl = inputData.getString(DATA_ENDPOINT) ?: return Result.failure()
        val endpoint = Endpoint(endpointUrl)
        val jobUid = inputData.getInt(DATA_JOB_UID, 0)
        val contentEntryVersionUid = inputData.getLong(DATA_CONTENTENTRYVERSION_UID, 0L)

        val contentManifestDownloadUseCase: ContentManifestDownloadUseCase = di.on(endpoint).direct
            .instance()
        val updateFailedTransferJobUseCase: UpdateFailedTransferJobUseCase by di.on(endpoint)
            .instance()

        return try {
            contentManifestDownloadUseCase(
                contentEntryVersionUid = contentEntryVersionUid,
                transferJobUid = jobUid,
            )
            Result.success()
        }catch(e: Throwable) {
            if(runAttemptCount < ContentManifestDownloadUseCase.DEFAULT_MAX_ATTEMPTS) {
                Result.retry()
            }else {
                withContext(NonCancellable) {
                    updateFailedTransferJobUseCase(jobUid)
                }

                Result.failure()
            }
        }

    }
}