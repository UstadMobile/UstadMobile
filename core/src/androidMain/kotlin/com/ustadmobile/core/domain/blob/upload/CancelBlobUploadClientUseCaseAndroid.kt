package com.ustadmobile.core.domain.blob.upload

import android.content.Context
import androidx.work.WorkManager
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.composites.TransferJobItemStatus

class CancelBlobUploadClientUseCaseAndroid(
    private val appContext: Context,
    private val learningSpace: LearningSpace,
    private val db: UmAppDatabase,
) : CancelBlobUploadClientUseCase{

    override suspend fun invoke(transferJobUid: Int) {
        db.transferJobDao().updateStatus(transferJobUid, TransferJobItemStatus.STATUS_CANCELLED)
        WorkManager.getInstance(appContext).cancelUniqueWork(
            "blob-upload-${learningSpace.url}-${transferJobUid}")
    }
}