package com.ustadmobile.core.domain.blob.upload

import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.TransferJob
import com.ustadmobile.lib.db.entities.TransferJobItem
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.transferjobitem.UpdateTransferJobItemEtagUseCase
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.ihttp.request.iRequestBuilder

abstract class AbstractEnqueueBlobUploadClientUseCase(
    private val db: UmAppDatabase,
    private val cache: UstadCache,
    private val updateTransferJobItemEtagUseCase: UpdateTransferJobItemEtagUseCase =
        UpdateTransferJobItemEtagUseCase(),
) : EnqueueBlobUploadClientUseCase {

    protected suspend fun createTransferJob(
        blobUrls: List<EnqueueBlobUploadClientUseCase.EnqueueBlobUploadItem>,
        batchUuid: String,
        tableId: Int,
        entityUid: Long,
    ): TransferJob {
        return db.withDoorTransactionAsync {
            val transferJob = TransferJob(
                tjUuid = batchUuid,
                tjType = TransferJob.TYPE_BLOB_UPLOAD,
                tjName = "",
                tjTimeCreated = systemTimeInMillis(),
                tjTableId = tableId,
                tjEntityUid = entityUid,
            )
            val jobUid = db.transferJobDao().insert(transferJob).toInt()

            blobUrls.forEach { enqueueUploadItem ->
                val httpResponse = cache.retrieve(iRequestBuilder(enqueueUploadItem.blobUrl))

                if(httpResponse != null) {
                    val transferJobItemUid = db.transferJobItemDao().insert(
                        TransferJobItem(
                            tjiTjUid = jobUid,
                            tjiSrc = enqueueUploadItem.blobUrl,
                            tjTotalSize = httpResponse.headers["content-length"]?.toLong() ?: 0,
                            tjiEntityUid =  enqueueUploadItem.entityUid,
                            tjiTableId = enqueueUploadItem.tableId,
                            tjiLockIdToRelease = enqueueUploadItem.retentionLockIdToRelease,
                        )
                    ).toInt()

                    updateTransferJobItemEtagUseCase(
                        db = db,
                        tableId = enqueueUploadItem.tableId,
                        entityUid = enqueueUploadItem.entityUid,
                        transferJobItemUid = transferJobItemUid
                    )
                }
            }

            transferJob.copy(
                tjUid = jobUid
            )
        }
    }

    companion object {

        const val DATA_LEARNINGSPACE = "endpoint"

        const val DATA_JOB_UID = "jobUid"

    }

}