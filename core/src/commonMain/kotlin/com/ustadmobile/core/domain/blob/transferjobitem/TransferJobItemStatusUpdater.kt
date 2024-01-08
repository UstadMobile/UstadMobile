package com.ustadmobile.core.domain.blob.transferjobitem

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.upload.BlobUploadClientUseCase
import com.ustadmobile.core.util.ext.lastDistinctBy
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Manages updating TransferJobItem status and progress periodically, thus reducing the number of
 * database transactions that run.
 *
 * This is used by the BlobUploadClientUseCase on JVM and Android and the SaveLocalUriAsBlobUseCase
 * on Javascript.
 */
class TransferJobItemStatusUpdater(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
    scope: CoroutineScope,
) {

    private val finished = atomic(false)

    private val progressUpdates = atomic(
        emptyList<BlobUploadClientUseCase.BlobUploadProgressUpdate>()
    )
    private val statusUpdates = atomic(
        emptyList<BlobUploadClientUseCase.BlobUploadStatusUpdate>()
    )

    private val updateJob = scope.launch {
        while(isActive) {
            delay(1000)
            commit()
        }
    }


    fun onProgressUpdate(update: BlobUploadClientUseCase.BlobUploadProgressUpdate) {
        progressUpdates.update { prev ->
            prev + update
        }
    }

    fun onStatusUpdate(update: BlobUploadClientUseCase.BlobUploadStatusUpdate) {
        statusUpdates.update { prev ->
            prev + update
        }
    }

    suspend fun commit(){
        val progressUpdatesToQueue =
            progressUpdates.getAndSet(emptyList())

        val statusUpdatesToQueue =
            statusUpdates.getAndSet(emptyList())

        val progressUpdatesToCommit = progressUpdatesToQueue.lastDistinctBy {
            it.uploadItem.transferJobItemUid
        }
        val statusUpdatesToCommit = statusUpdatesToQueue.lastDistinctBy {
            it.uploadItem.transferJobItemUid
        }

        val repoNodeId = (repo as? DoorDatabaseRepository)?.remoteNodeIdOrFake()

        db.takeIf {
            progressUpdatesToCommit.isNotEmpty() || statusUpdatesToCommit.isNotEmpty()
        }?.withDoorTransactionAsync {
            progressUpdatesToCommit.forEach {
                db.transferJobItemDao.updateTransferredProgress(
                    jobItemUid = it.uploadItem.transferJobItemUid,
                    transferred = it.bytesTransferred,
                )
            }

            statusUpdatesToCommit.forEach {
                db.transferJobItemDao.updateStatus(
                    jobItemUid = it.uploadItem.transferJobItemUid,
                    status = it.status
                )

                //If the blob upload is complete and associated with an entityUid, then put in an
                // OutgoingReplication so that the new value is sent to the server.
                if(it.status == TransferJobItemStatus.COMPLETE.value && repoNodeId != null) {
                    db.transferJobItemDao.insertOutgoingReplicationForTransferJobItemIfDone(
                        destNodeId = repoNodeId,
                        transferJobItemUid = it.uploadItem.transferJobItemUid
                    )
                }
            }
        }
    }

    suspend fun onFinished() {
        if(!finished.getAndSet(true)) {
            updateJob.cancel()
            commit()
        }
    }


}