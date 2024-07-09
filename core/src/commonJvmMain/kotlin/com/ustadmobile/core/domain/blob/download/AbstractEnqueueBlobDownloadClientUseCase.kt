package com.ustadmobile.core.domain.blob.download

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
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
                db.transferJobDao().findByUid(existingTransferJobId)
                    ?: throw IllegalArgumentException("Transfer job does not exist")
            }else {
                val newJob = TransferJob(
                    tjStatus = TransferJobItemStatus.STATUS_QUEUED_INT,
                    tjType = TransferJob.TYPE_DOWNLOAD,
                    tjTimeCreated = systemTimeInMillis(),
                )
                newJob.copy(tjUid = db.transferJobDao().insert(newJob).toInt())
            }
            val transferJobItems = items.map { item ->
                TransferJobItem(
                    tjiTjUid = transferJob.tjUid,
                    tjTotalSize = item.expectedSize ?: 0L,
                    tjiSrc = item.url,
                    tjiType = TransferJob.TYPE_DOWNLOAD,
                    tjiEntityUid = item.entityUid,
                    tjiTableId = item.tableId,
                    tjiStatus = TransferJobItemStatus.STATUS_QUEUED_INT,
                    tjiPartialTmpFile = item.partialTmpFile,
                )
            }
            db.transferJobItemDao().insertList(transferJobItems)
            transferJob
        }
    }

    companion object {

        const val DATA_ENDPOINT = "endpoint"

        const val DATA_JOB_UID = "jobUid"

        const val UNIQUE_NAME_PREFIX = "blob-download-"

        fun uniqueNameFor(endpoint: Endpoint, transferJobId: Int) : String {
            return "$UNIQUE_NAME_PREFIX${endpoint.url}-$transferJobId"
        }

    }

}