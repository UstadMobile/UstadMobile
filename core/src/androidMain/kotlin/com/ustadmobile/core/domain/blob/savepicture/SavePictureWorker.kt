package com.ustadmobile.core.domain.blob.savepicture

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ustadmobile.core.account.LearningSpace
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase.Companion.DATA_LEARNINGSPACE
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase.Companion.DATA_TABLE_ID
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase.Companion.DATA_ENTITY_UID
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase.Companion.DATA_LOCAL_URI
class SavePictureWorker(
    context: Context,
    params: WorkerParameters
): CoroutineWorker(context, params){

    private val di: DI by closestDI { applicationContext }

    override suspend fun doWork(): Result {
        val learningSpaceUrl = inputData.getString(DATA_LEARNINGSPACE)
            ?: throw IllegalArgumentException("No endpoint")
        val learningSpace = LearningSpace(learningSpaceUrl)
        val savePictureUseCase: SavePictureUseCase = di.on(learningSpace).direct.instance()
        savePictureUseCase(
            tableId = inputData.getInt(DATA_TABLE_ID, 0),
            entityUid = inputData.getLong(DATA_ENTITY_UID, 0),
            pictureUri = inputData.getString(DATA_LOCAL_URI)
        )

        return Result.success()
    }


}