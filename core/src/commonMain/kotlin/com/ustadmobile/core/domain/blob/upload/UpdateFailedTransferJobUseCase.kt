package com.ustadmobile.core.domain.blob.upload

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.composites.TransferJobItemStatus

/**
 * Mark a TransferJob as failed, and mark any TransferJobItem that has not yet completed as failed.
 * Used on Android/JVM via BlobUploadClientUseCaseJvm and used on JS via SaveLocalUriAsBlobUseCase
 */
class UpdateFailedTransferJobUseCase (
    private val db: UmAppDatabase
) {

    suspend operator fun invoke(jobUid : Int) {
        db.withDoorTransactionAsync {
            db.transferJobItemDao().updateStatusIfNotCompleteForAllInJob(
                jobUid = jobUid,
                status = TransferJobItemStatus.FAILED.value
            )
            db.transferJobDao().updateStatus(
                jobUid = jobUid,
                status = TransferJobItemStatus.FAILED.value
            )
        }
    }

}