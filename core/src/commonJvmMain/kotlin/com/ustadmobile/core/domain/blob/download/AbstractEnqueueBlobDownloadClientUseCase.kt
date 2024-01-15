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
        items: List<EnqueueBlobDownloadClientUseCase.EnqueueBlobDownloadItem>
    ): TransferJob {
        return db.withDoorTransactionAsync {
            val transferJob = TransferJob(
                tjStatus = TransferJobItemStatus.STATUS_QUEUED_INT,
                tjType = TransferJob.TYPE_DOWNLOAD,
            )
            val jobUid = db.transferJobDao.insert(transferJob).toInt()
            val transferJobItems = items.map { item ->
                TransferJobItem(
                    tjiTjUid = jobUid,
                    tjTotalSize = item.totalSize ?: 0L,
                    tjiSrc = item.url,
                    tjiType = TransferJob.TYPE_DOWNLOAD,
                    tjiEntityUid = item.entityUid,
                    tjiTableId = item.tableId,
                    tjiStatus = TransferJobItemStatus.STATUS_QUEUED_INT,
                )
            }
            db.transferJobItemDao.insertList(transferJobItems)
            transferJob.copy(
                tjUid = jobUid
            )
        }
    }

    companion object {

        const val DATA_ENDPOINT = "endpoint"

        const val DATA_JOB_UID = "jobUid"

    }

}