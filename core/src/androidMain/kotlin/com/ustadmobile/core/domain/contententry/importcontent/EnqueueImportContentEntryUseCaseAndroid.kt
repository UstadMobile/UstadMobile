package com.ustadmobile.core.domain.contententry.importcontent

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.isRemote
import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.ContentEntryImportJob

/**
 * @param enqueueRemoteImport delegate used when the user is importing a remote link link eg http/https
 */
class EnqueueImportContentEntryUseCaseAndroid(
    private val db: UmAppDatabase,
    private val appContext: Context,
    private val endpoint: Endpoint,
    private val enqueueRemoteImport: EnqueueContentEntryImportUseCase,
) : EnqueueContentEntryImportUseCase {

    override suspend fun invoke(
        contentJobItem: ContentEntryImportJob,
    ) {
        val sourceUri = DoorUri.parse(contentJobItem.sourceUri!!)
        if(sourceUri.isRemote()) {
            enqueueRemoteImport(contentJobItem)
        }else{
            val uid = db.contentEntryImportJobDao().insertJobItem(contentJobItem)

            val jobData = Data.Builder()
                .putString(EnqueueContentEntryImportUseCase.DATA_ENDPOINT, endpoint.url)
                .putLong(EnqueueContentEntryImportUseCase.DATA_JOB_UID, uid)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<ImportContentEntryWorker>()
                .setInputData(jobData)
                .build()

            WorkManager.getInstance(appContext).enqueueUniqueWork(
                EnqueueContentEntryImportUseCase.uniqueNameFor(endpoint, uid),
                ExistingWorkPolicy.REPLACE, workRequest)
        }
    }
}