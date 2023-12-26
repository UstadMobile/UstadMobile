package com.ustadmobile.core.domain.blob.savepicture

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ustadmobile.core.account.Endpoint

class EnqueueSavePictureUseCaseAndroid(
    private val appContext: Context,
    private val endpoint: Endpoint,
): EnqueueSavePictureUseCase {

    override suspend fun invoke(entityUid: Long, tableId: Int, pictureUri: String?) {
        val workName = "${endpoint.url}-$tableId-$entityUid"
        val inputData = Data.Builder()
            .putString(SavePictureWorker.DATA_ENDPOINT, endpoint.url)
            .putLong(SavePictureWorker.DATA_ENTITY_UID, entityUid)
            .putInt(SavePictureWorker.DATA_TABLE_ID, tableId)
            .putString(SavePictureWorker.DATA_LOCAL_URI, pictureUri)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<SavePictureWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(appContext).enqueueUniqueWork(
            workName, ExistingWorkPolicy.REPLACE, workRequest
        )
    }
}