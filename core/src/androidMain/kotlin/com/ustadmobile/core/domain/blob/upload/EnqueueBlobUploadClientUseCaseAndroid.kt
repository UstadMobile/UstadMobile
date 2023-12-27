package com.ustadmobile.core.domain.blob.upload

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.libcache.UstadCache

class EnqueueBlobUploadClientUseCaseAndroid(
    private val appContext: Context,
    private val endpoint: Endpoint,
    db: UmAppDatabase,
    cache: UstadCache,
) : AbstractEnqueueBlobUploadClientUseCase(
    db = db, cache = cache,
){
    override suspend fun invoke(blobUrls: List<String>, batchUuid: String, chunkSize: Int) {
        val transferJob = createTransferJob(blobUrls, batchUuid)
        val jobData = Data.Builder()
            .putString(DATA_ENDPOINT, endpoint.url)
            .putInt(DATA_JOB_UID, transferJob.tjUid)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<BlobUploadClientWorker>()
            .setInputData(jobData)
            .setConstraints(Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            ).build()

        WorkManager.getInstance(appContext).enqueueUniqueWork(
            "blob-upload-${endpoint.url}-${transferJob.tjUid}",
            ExistingWorkPolicy.REPLACE, workRequest)
    }
}