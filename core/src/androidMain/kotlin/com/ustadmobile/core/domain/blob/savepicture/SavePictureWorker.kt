package com.ustadmobile.core.domain.blob.savepicture

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ustadmobile.core.account.Endpoint
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class SavePictureWorker(
    context: Context,
    params: WorkerParameters
): CoroutineWorker(context, params){

    private val di: DI by closestDI { applicationContext }

    override suspend fun doWork(): Result {
        val endpointUrl = inputData.getString(DATA_ENDPOINT)
            ?: throw IllegalArgumentException("No endpoint")
        val endpoint = Endpoint(endpointUrl)
        val savePictureUseCase: SavePictureUseCase = di.on(endpoint).direct.instance()
        savePictureUseCase(
            tableId = inputData.getInt(DATA_TABLE_ID, 0),
            entityUid = inputData.getLong(DATA_ENTITY_UID, 0),
            pictureUri = inputData.getString(DATA_LOCAL_URI)
        )

        return Result.success()
    }

    companion object {

        const val DATA_ENDPOINT = "endpoint"

        const val DATA_TABLE_ID = "tableId"

        const val DATA_ENTITY_UID = "entityUid"

        const val DATA_LOCAL_URI = "localUri"

    }
}