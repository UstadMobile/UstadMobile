package com.ustadmobile.core.domain.blob.download

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.db.UmAppDatabase
import java.util.concurrent.TimeUnit

class EnqueueContentManifestDownloadJobUseCaseAndroid(
    private val appContext: Context,
    private val learningSpace: LearningSpace,
    db: UmAppDatabase
): AbstractEnqueueContentManifestDownloadUseCase(db) {

    override suspend fun invoke(
        contentEntryVersionUid: Long,
        offlineItemUid: Long,
    ) {
        val transferJob = createTransferJob(
            contentEntryVersionUid = contentEntryVersionUid,
            offlineItemUid = offlineItemUid
        )

        val jobData = Data.Builder()
            .putString(DATA_LEARNINGSPACE, learningSpace.url)
            .putInt(DATA_JOB_UID, transferJob.tjUid)
            .putLong(DATA_CONTENTENTRYVERSION_UID, contentEntryVersionUid)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ContentManifestDownloadWorker>()
            .setInputData(jobData)
            .addTag("offlineitem-${learningSpace.url}-$offlineItemUid")
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            ).build()

        WorkManager.getInstance(appContext).enqueueUniqueWork(
            uniqueNameFor(learningSpace, transferJob.tjUid),
            ExistingWorkPolicy.REPLACE, workRequest)
    }
}