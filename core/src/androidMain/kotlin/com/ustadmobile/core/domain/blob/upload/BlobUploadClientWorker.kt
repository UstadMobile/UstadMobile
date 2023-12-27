package com.ustadmobile.core.domain.blob.upload

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ustadmobile.core.account.Endpoint
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
        val endpointUrl = inputData.getString(AbstractEnqueueBlobUploadClientUseCase.DATA_ENDPOINT)
            ?: return Result.failure()
        val jobUid = inputData.getInt(AbstractEnqueueBlobUploadClientUseCase.DATA_JOB_UID, 0)
        val blobUploadClientUseCase: BlobUploadClientUseCase = di.on(Endpoint(endpointUrl)).direct.instance()
        blobUploadClientUseCase(
            transferJobUid = jobUid
        )
        return Result.success()
    }
}