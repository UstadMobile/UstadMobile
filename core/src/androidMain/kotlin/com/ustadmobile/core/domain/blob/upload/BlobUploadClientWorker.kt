package com.ustadmobile.core.domain.blob.upload

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ustadmobile.core.account.LearningSpace
import io.github.aakira.napier.Napier
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class BlobUploadClientWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params){

    private val di: DI by closestDI { applicationContext }

    override suspend fun doWork(): Result {
        val learningSpaceUrl = inputData.getString(AbstractEnqueueBlobUploadClientUseCase.DATA_LEARNINGSPACE)
            ?: return Result.failure()
        val jobUid = inputData.getInt(AbstractEnqueueBlobUploadClientUseCase.DATA_JOB_UID, 0)
        val endpoint = LearningSpace(learningSpaceUrl)
        val blobUploadClientUseCase: BlobUploadClientUseCase = di.on(endpoint).direct
            .instance()
        val updateFailedTransferJobUseCase: UpdateFailedTransferJobUseCase by di.on(endpoint).instance()

        return try {
            blobUploadClientUseCase(
                transferJobUid = jobUid
            )
            Result.success()
        }catch(e: Throwable) {
            val canRetry = this.runAttemptCount < BlobUploadClientUseCaseJvm.MAX_ATTEMPTS_DEFAULT
            if(canRetry) {
                Napier.w("BlobUploadClientWorker ($jobUid): FAIL - attempt $runAttemptCount. Will retry")
                return Result.retry()
            }else {
                try {
                    Napier.e("BlobUploadClientWorker ($jobUid) FAIL - attempt $runAttemptCount . No more attempts")
                    updateFailedTransferJobUseCase(jobUid)
                    Napier.e("BlobUploadClientWorker ($jobUid) FAIL - attempt $runAttemptCount . Database updated")
                }catch(e: Throwable) {
                    e.printStackTrace()
                }

                return Result.failure()
            }
        }
    }
}