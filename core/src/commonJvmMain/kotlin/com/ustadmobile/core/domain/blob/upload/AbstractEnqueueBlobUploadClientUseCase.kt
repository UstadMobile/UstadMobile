package com.ustadmobile.core.domain.blob.upload

import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.TransferJob
import com.ustadmobile.lib.db.entities.TransferJobItem
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.request.requestBuilder

abstract class AbstractEnqueueBlobUploadClientUseCase(
    private val db: UmAppDatabase,
    private val cache: UstadCache,
) : EnqueueBlobUploadClientUseCase {


    //Create transferjob and transferjob items in the database.
    protected suspend fun createTransferJob(
        blobUrls: List<EnqueueBlobUploadClientUseCase.EnqueueBlobUploadItem>,
        batchUuid: String,
    ): TransferJob {
        return db.withDoorTransactionAsync {
            val transferJob = TransferJob(
                tjUuid = batchUuid,
                tjName = ""
            )
            val jobUid = db.transferJobDao.insert(transferJob).toInt()

            db.transferJobItemDao.insertList(
                blobUrls.mapNotNull { enqueueUploadItem ->
                    cache.retrieve(requestBuilder(enqueueUploadItem.blobUrl))?.let { httpResponse ->
                        TransferJobItem(
                            tjiTjUid = jobUid,
                            tjiSrc = enqueueUploadItem.blobUrl,
                            tjTotalSize = httpResponse.headers["content-length"]?.toLong() ?: 0,
                            tjiEntityUid =  enqueueUploadItem.entityUid,
                            tjiTableId = enqueueUploadItem.tableId,
                        )
                    }
                }
            )

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