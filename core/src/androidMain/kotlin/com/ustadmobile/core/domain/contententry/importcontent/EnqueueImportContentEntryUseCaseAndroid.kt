package com.ustadmobile.core.domain.contententry.importcontent

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.ContentEntryImportJob

class EnqueueImportContentEntryUseCaseAndroid(
    private val db: UmAppDatabase,
    private val appContext: Context,
    private val endpoint: Endpoint,
) : EnqueueContentEntryImportUseCase {

    override suspend fun invoke(contentJobItem: ContentEntryImportJob) {
        val uid = db.contentEntryImportJobDao.insertJobItem(contentJobItem)

        val jobData = Data.Builder()
            .putString(EnqueueContentEntryImportUseCase.DATA_ENDPOINT, endpoint.url)
            .putLong(EnqueueContentEntryImportUseCase.DATA_JOB_UID, uid)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ImportContentEntryWorker>()
            .setInputData(jobData)
            .build()

        val workName = "import-content-entry-${endpoint.url}-$uid"
        WorkManager.getInstance(appContext).enqueueUniqueWork(workName,
            ExistingWorkPolicy.REPLACE, workRequest)
    }
}