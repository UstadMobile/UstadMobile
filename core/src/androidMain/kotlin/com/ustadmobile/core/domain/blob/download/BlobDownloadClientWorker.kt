package com.ustadmobile.core.domain.blob.download

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.blob.download.AbstractEnqueueBlobDownloadClientUseCase.Companion.DATA_ENDPOINT
import com.ustadmobile.core.domain.blob.download.AbstractEnqueueBlobDownloadClientUseCase.Companion.DATA_JOB_UID
import com.ustadmobile.core.domain.blob.upload.UpdateFailedTransferJobUseCase
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class BlobDownloadClientWorker(
    context: Context,
    params: WorkerParameters
): CoroutineWorker(context, params ) {

    private val di: DI by closestDI { applicationContext }

    override suspend fun doWork(): Result {
        val endpointUrl = inputData.getString(DATA_ENDPOINT) ?: return Result.failure()
        val jobUid = inputData.getInt(DATA_JOB_UID, 0)

        val endpoint = Endpoint(endpointUrl)
        val blobDownloadUseCase: BlobDownloadClientUseCase = di.on(endpoint).direct.instance()
        val updateFailedTransferJobUseCase: UpdateFailedTransferJobUseCase by di.on(endpoint)
            .instance()

        return try {
            blobDownloadUseCase(jobUid)
            Result.success()
        }catch(e: Throwable) {
            val canRetry = runAttemptCount < BlobDownloadClientUseCase.DEFAULT_MAX_ATTEMPTS
            if(canRetry) {
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