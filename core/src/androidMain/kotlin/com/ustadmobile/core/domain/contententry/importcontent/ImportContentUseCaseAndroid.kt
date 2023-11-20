package com.ustadmobile.core.domain.contententry.importcontent

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.ContentJobManager
import com.ustadmobile.core.contentjob.ContentJobRunnerWorker
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentJobItem

class ImportContentUseCaseAndroid(
    private val endpoint: Endpoint,
    private val appContext: Context,
    private val db: UmAppDatabase,
) : ImportContentUseCase {

    override suspend fun invoke(contentJob: ContentJob, contentJobItem: ContentJobItem) {
        val contentJobUid = db.withDoorTransactionAsync {
            val contentJobId = db.contentJobDao.insertAsync(contentJob)
            db.contentJobItemDao.insertJobItem(
                contentJobItem.copy(
                    cjiJobUid = contentJobId,
                    cjiLastModified = systemTimeInMillis(),
                )
            )

            contentJobId
        }

        val inputData = Data.Builder()
            .putString(ContentJobManager.KEY_ENDPOINT, endpoint.url)
            .putLong(ContentJobManager.KEY_CONTENTJOB_UID, contentJobUid)
            .build()

        val request = OneTimeWorkRequest.Builder(ContentJobRunnerWorker::class.java)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(appContext)
            .enqueueUniqueWork("contentjob-${endpoint.url}-$contentJobUid",
                ExistingWorkPolicy.REPLACE, request)

    }
}