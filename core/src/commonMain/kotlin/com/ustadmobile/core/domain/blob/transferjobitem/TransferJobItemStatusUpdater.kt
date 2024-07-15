package com.ustadmobile.core.domain.blob.transferjobitem

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.BlobTransferProgressUpdate
import com.ustadmobile.core.domain.blob.BlobTransferStatusUpdate
import com.ustadmobile.core.util.ext.lastDistinctBy
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import io.github.aakira.napier.Napier
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
 * This is used by the BlobUploadClientUseCase and BlobDownloadClientUseCaseCommonJvm on JVM and
 * Android and the SaveLocalUriAsBlobUseCase on Javascript.
 */
class TransferJobItemStatusUpdater(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
    scope: CoroutineScope,
    private val commitInterval: Long = 500,
) {

    private val finished = atomic(false)

    private val progressUpdates = atomic(
        emptyList<BlobTransferProgressUpdate>()
    )
    private val statusUpdates = atomic(
        emptyList<BlobTransferStatusUpdate>()
    )

    private val updateJob = scope.launch {
        while(isActive) {
            delay(commitInterval)
            commit()
        }
    }


    fun onProgressUpdate(update: BlobTransferProgressUpdate) {
        progressUpdates.update { prev ->
            prev + update
        }
    }

    fun onStatusUpdate(update: BlobTransferStatusUpdate) {
        statusUpdates.update { prev ->
            prev + update
        }
    }

    /**
     * @param updateTransferJobStatusUid a transferjobuid for which we should set TransferJob.tjStatus to
     *        complete if all related TransferJobItem.tjiStatus(s) are complete
     *
     */
    suspend fun commit(
        updateTransferJobStatusUid: Int = 0
    ){
        val progressUpdatesToQueue =
            progressUpdates.getAndSet(emptyList())

        val statusUpdatesToQueue =
            statusUpdates.getAndSet(emptyList())

        val progressUpdatesToCommit = progressUpdatesToQueue.lastDistinctBy {
            it.transferItem.transferJobItemUid
        }
        val statusUpdatesToCommit = statusUpdatesToQueue.lastDistinctBy {
            it.transferItem.transferJobItemUid
        }

        val repoNodeId = (repo as? DoorDatabaseRepository)?.remoteNodeIdOrFake()

        db.takeIf {
            progressUpdatesToCommit.isNotEmpty() || statusUpdatesToCommit.isNotEmpty()
                    || updateTransferJobStatusUid != 0
        }?.withDoorTransactionAsync {
            progressUpdatesToCommit.forEach {
                db.transferJobItemDao().updateTransferredProgress(
                    jobItemUid = it.transferItem.transferJobItemUid,
                    transferred = it.bytesTransferred,
                )
            }

            statusUpdatesToCommit.forEach {
                db.transferJobItemDao().updateStatus(
                    jobItemUid = it.transferItem.transferJobItemUid,
                    status = it.status
                )

                //If the blob upload is complete and associated with an entityUid, then put in an
                // OutgoingReplication so that the new value is sent to the server.
                if(it.status == TransferJobItemStatus.COMPLETE.value && repoNodeId != null) {
                    db.transferJobItemDao().insertOutgoingReplicationForTransferJobItemIfDone(
                        destNodeId = repoNodeId,
                        transferJobItemUid = it.transferItem.transferJobItemUid
                    )
                }
            }

            if(updateTransferJobStatusUid != 0) {
                val numUpdates = db.transferJobDao().updateStatusIfComplete(
                    jobUid = updateTransferJobStatusUid
                )
                Napier.d { "TransferJobItemStatusUpdater: update status complete for " +
                        "$updateTransferJobStatusUid updates=$numUpdates" }
            }
        }
    }

    suspend fun onFinished(
        updateTransferJobStatusUid: Int = 0
    ) {
        if(!finished.getAndSet(true)) {
            updateJob.cancel()
            commit(updateTransferJobStatusUid)
        }
    }


}