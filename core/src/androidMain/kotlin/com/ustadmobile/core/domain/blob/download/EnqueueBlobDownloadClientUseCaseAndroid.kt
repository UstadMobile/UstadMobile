package com.ustadmobile.core.domain.blob.download

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import java.util.concurrent.TimeUnit

class EnqueueBlobDownloadClientUseCaseAndroid(
    private val appContext: Context,
    private val endpoint: Endpoint,
    db: UmAppDatabase,
) : AbstractEnqueueBlobDownloadClientUseCase(db){

    override suspend fun invoke(
        items: List<EnqueueBlobDownloadClientUseCase.EnqueueBlobDownloadItem>
    ) {
        val transferJob = createTransferJob(items)
        val jobData = Data.Builder()
            .putString(DATA_ENDPOINT, endpoint.url)
            .putInt(DATA_JOB_UID, transferJob.tjUid)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<BlobDownloadClientWorker>()
            .setInputData(jobData)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
            .setConstraints(
                Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            ).build()

        WorkManager.getInstance(appContext).enqueueUniqueWork(
            "blob-download-${endpoint.url}-${transferJob.tjUid}",
            ExistingWorkPolicy.REPLACE, workRequest)
    }
}