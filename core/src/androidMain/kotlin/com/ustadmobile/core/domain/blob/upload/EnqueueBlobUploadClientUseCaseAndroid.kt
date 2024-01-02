package com.ustadmobile.core.domain.blob.upload

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
import com.ustadmobile.libcache.UstadCache
import java.util.concurrent.TimeUnit

class EnqueueBlobUploadClientUseCaseAndroid(
    private val appContext: Context,
    private val endpoint: Endpoint,
    db: UmAppDatabase,
    cache: UstadCache,
) : AbstractEnqueueBlobUploadClientUseCase(
    db = db, cache = cache,
){
    override suspend fun invoke(
        items: List<EnqueueBlobUploadClientUseCase.EnqueueBlobUploadItem>,
        batchUuid: String,
        chunkSize: Int
    ) {
        val transferJob = createTransferJob(items, batchUuid)
        val jobData = Data.Builder()
            .putString(DATA_ENDPOINT, endpoint.url)
            .putInt(DATA_JOB_UID, transferJob.tjUid)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<BlobUploadClientWorker>()
            .setInputData(jobData)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
            .setConstraints(Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            ).build()

        WorkManager.getInstance(appContext).enqueueUniqueWork(
            "blob-upload-${endpoint.url}-${transferJob.tjUid}",
            ExistingWorkPolicy.REPLACE, workRequest)
    }
}