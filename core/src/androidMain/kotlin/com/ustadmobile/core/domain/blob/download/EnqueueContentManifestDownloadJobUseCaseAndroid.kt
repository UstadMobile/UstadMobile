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
import com.ustadmobile.core.domain.blob.download.EnqueueBlobDownloadClientUseCase.Companion.KEY_CONNECTIVITY_REQUIRED
import com.ustadmobile.core.domain.blob.getmanifest.GetContentManifestUseCase
import com.ustadmobile.core.domain.localsharing.checkcontentavailability.CheckContentLocalAvailabilityUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeUnit

class EnqueueContentManifestDownloadJobUseCaseAndroid(
    private val appContext: Context,
    private val learningSpace: LearningSpace,
    db: UmAppDatabase,
    private val getContentManifestUseCase: GetContentManifestUseCase?,
    private val checkContentLocalAvailabilityUseCase: CheckContentLocalAvailabilityUseCase?,
): AbstractEnqueueContentManifestDownloadUseCase(db) {

    override suspend fun invoke(
        contentEntryVersionUid: Long,
        offlineItemUid: Long,
    ) {
        val transferJob = createTransferJob(
            contentEntryVersionUid = contentEntryVersionUid,
            offlineItemUid = offlineItemUid,
        )

        val getContentManifestUseCaseVal = getContentManifestUseCase
        val checkContentLocalAvailabilityUseCaseVal = checkContentLocalAvailabilityUseCase

        val locallyAvailable = if(getContentManifestUseCaseVal != null &&
            checkContentLocalAvailabilityUseCaseVal != null
        ) {
            try {
                withTimeout(GET_MANIFEST_TIMEOUT) {
                    checkContentLocalAvailabilityUseCaseVal(
                        getContentManifestUseCaseVal(contentEntryVersionUid).contentEntryVersion
                    )
                }
            }catch(e: Throwable) {
                Napier.w("EnqueueContentManifestDownloadUseCase: could not check for availability", e)
                false
            }
        }else {
            false
        }

        val jobData = Data.Builder()
            .putString(DATA_LEARNINGSPACE, learningSpace.url)
            .putInt(DATA_JOB_UID, transferJob.tjUid)
            .putLong(DATA_CONTENTENTRYVERSION_UID, contentEntryVersionUid)
            .putBoolean(KEY_CONNECTIVITY_REQUIRED, !locallyAvailable)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ContentManifestDownloadWorker>()
            .setInputData(jobData)
            .addTag("offlineitem-${learningSpace.url}-$offlineItemUid")
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
            .let {
                if(!locallyAvailable) {
                    it.setConstraints(
                        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                    )
                }else {
                    it
                }
            }.build()

        WorkManager.getInstance(appContext).enqueueUniqueWork(
            uniqueNameFor(learningSpace, transferJob.tjUid),
            ExistingWorkPolicy.REPLACE, workRequest)
    }

    companion object {

        const val GET_MANIFEST_TIMEOUT = 5_000L

    }
}