package com.ustadmobile.core.domain.blob.download

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import com.ustadmobile.lib.db.entities.TransferJob
import com.ustadmobile.lib.db.entities.TransferJobItem

abstract class AbstractEnqueueBlobDownloadClientUseCase(
    private val db: UmAppDatabase,
): EnqueueBlobDownloadClientUseCase {

    protected suspend fun createTransferJob(
        items: List<EnqueueBlobDownloadClientUseCase.EnqueueBlobDownloadItem>,
        existingTransferJobId: Int = 0
    ): TransferJob {
        return db.withDoorTransactionAsync {
            val transferJob = if(existingTransferJobId != 0) {
                val newJob = TransferJob(
                    tjStatus = TransferJobItemStatus.STATUS_QUEUED_INT,
                    tjType = TransferJob.TYPE_DOWNLOAD,
                )
                newJob.copy(tjUid = db.transferJobDao.insert(newJob).toInt())
            }else {
                db.transferJobDao.findByUid(existingTransferJobId)
                    ?: throw IllegalArgumentException("Transfer job does not exist")
            }
            val transferJobItems = items.map { item ->
                TransferJobItem(
                    tjiTjUid = transferJob.tjUid,
                    tjTotalSize = item.totalSize ?: 0L,
                    tjiSrc = item.url,
                    tjiType = TransferJob.TYPE_DOWNLOAD,
                    tjiEntityUid = item.entityUid,
                    tjiTableId = item.tableId,
                    tjiStatus = TransferJobItemStatus.STATUS_QUEUED_INT,
                )
            }
            db.transferJobItemDao.insertList(transferJobItems)
            transferJob
        }
    }

    companion object {

        const val DATA_ENDPOINT = "endpoint"

        const val DATA_JOB_UID = "jobUid"

    }

}