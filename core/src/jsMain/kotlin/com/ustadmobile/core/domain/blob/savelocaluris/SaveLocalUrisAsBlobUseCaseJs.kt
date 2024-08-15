package com.ustadmobile.core.domain.blob.savelocaluris

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.BlobTransferJobItem
import com.ustadmobile.core.domain.blob.BlobTransferProgressUpdate
import com.ustadmobile.core.domain.blob.BlobTransferStatusUpdate
import com.ustadmobile.core.domain.blob.transferjobitem.TransferJobItemStatusUpdater
import com.ustadmobile.core.domain.blob.transferjobitem.UpdateTransferJobItemEtagUseCase
import com.ustadmobile.core.domain.blob.upload.BlobUploadClientUseCase.Companion.BLOB_RESPONSE_HEADER_PREFIX
import com.ustadmobile.core.domain.upload.ChunkedUploadClientLocalUriUseCase
import com.ustadmobile.core.util.stringvalues.asIStringValues
import com.ustadmobile.core.util.uuid.randomUuidAsString
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import com.ustadmobile.lib.db.entities.TransferJob
import com.ustadmobile.lib.db.entities.TransferJobItem
import io.github.aakira.napier.Napier
import js.promise.await
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import web.http.fetchAsync

class SaveLocalUrisAsBlobUseCaseJs(
    private val chunkedUploadClientLocalUriUseCase: ChunkedUploadClientLocalUriUseCase,
    private val learningSpace: LearningSpace,
    private val json: Json,
    private val db: UmAppDatabase,
    private val updateTransferJobItemEtagUseCase: UpdateTransferJobItemEtagUseCase =
        UpdateTransferJobItemEtagUseCase(),
): SaveLocalUrisAsBlobsUseCase {

    //UriToSave and TransferJobItem
    data class UriToSaveQueueItem(
        val uriToSaveItem: SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem,
        val transferJobItem: TransferJobItem,
    )

    override suspend fun invoke(
        localUrisToSave: List<SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem>,
        onTransferJobItemCreated: (SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem, TransferJobItem) -> Unit,
    ): List<SaveLocalUrisAsBlobsUseCase.SavedBlob> {
        val uriToSaveQueueItems = db.withDoorTransactionAsync {
            val transferJobUid = db.transferJobDao().insert(TransferJob()).toInt()

            localUrisToSave.map { localUriToSaveItem ->
                val itemSize = fetchAsync(localUriToSaveItem.localUri).await()
                    .blob().await().size.toLong()

                val transferJobItem = TransferJobItem(
                    tjiTjUid = transferJobUid,
                    tjiSrc = localUriToSaveItem.localUri,
                    tjTotalSize = itemSize,
                    tjiTableId = localUriToSaveItem.tableId,
                    tjiEntityUid = localUriToSaveItem.entityUid,
                )
                val transferJobItemUid = db.transferJobItemDao().insert(transferJobItem).toInt()

                onTransferJobItemCreated(
                    localUriToSaveItem,
                    transferJobItem.copy(tjiUid = transferJobItemUid)
                )

                updateTransferJobItemEtagUseCase(
                    db = db,
                    tableId = localUriToSaveItem.tableId,
                    entityUid = localUriToSaveItem.entityUid,
                    transferJobItemUid = transferJobItemUid
                )

                UriToSaveQueueItem(
                    uriToSaveItem = localUriToSaveItem,
                    transferJobItem = transferJobItem.copy(tjiUid = transferJobItemUid)
                )
            }
        }

        return coroutineScope {
            val transferJobItemStatusUpdater = TransferJobItemStatusUpdater(
                db = db,
                repo = null,
                scope = this
            )

            try {
                val savedBlobs = uriToSaveQueueItems.map { uriToSaveQueueItem ->
                    val uploadItem = BlobTransferJobItem(
                        blobUrl = "",
                        transferJobItemUid = uriToSaveQueueItem.transferJobItem.tjiUid,
                    )
                    transferJobItemStatusUpdater.onStatusUpdate(
                        BlobTransferStatusUpdate(
                            uploadItem, TransferJobItemStatus.IN_PROGRESS.value
                        )
                    )

                    val response = chunkedUploadClientLocalUriUseCase(
                        uploadUuid = randomUuidAsString(),
                        localUri = DoorUri.parse(uriToSaveQueueItem.uriToSaveItem.localUri),
                        remoteUrl = "${learningSpace.url}api/blob/upload-item",
                        lastChunkHeaders = buildMap {
                            uriToSaveQueueItem.uriToSaveItem.mimeType?.also { blobMimeType ->
                                put("${BLOB_RESPONSE_HEADER_PREFIX}Content-Type", listOf(blobMimeType))
                            }
                        }.asIStringValues(),
                        onProgress = {
                            transferJobItemStatusUpdater.onProgressUpdate(
                                BlobTransferProgressUpdate(
                                    transferItem = uploadItem,
                                    bytesTransferred = it.bytesTransferred,
                                )
                            )
                        }
                    )

                    val responseJsonStr = response.body
                        ?: throw IllegalStateException("SaveLocalUrisAsBlobUseCaseJs: no response body!")
                    transferJobItemStatusUpdater.onStatusUpdate(
                        BlobTransferStatusUpdate(
                            uploadItem, TransferJobItemStatus.COMPLETE.value
                        )
                    )

                    val savedBlob = json.decodeFromString(
                        SaveLocalUrisAsBlobsUseCase.SavedBlob.serializer(), responseJsonStr
                    )
                    Napier.d("SaveLocalUrisAsBlobUseCaseJs: upload complete: " +
                            "${uriToSaveQueueItem.uriToSaveItem.localUri} stored as ${savedBlob.blobUrl}")

                    savedBlob.copy(
                        entityUid = uriToSaveQueueItem.uriToSaveItem.entityUid,
                        tableId = uriToSaveQueueItem.uriToSaveItem.tableId,
                        localUri = uriToSaveQueueItem.uriToSaveItem.localUri,
                    )
                }

                transferJobItemStatusUpdater.onFinished()

                savedBlobs
            }catch(e: Throwable) {
                Napier.e("SaveLocalUriAsBlobUseCaseJs: exception uploading", e)
                //Currently no retry on web version: upload has failed.
                withContext(NonCancellable) {
                    db.withDoorTransactionAsync {
                        transferJobItemStatusUpdater.onFinished()
                        db.transferJobItemDao().updateStatusIfNotCompleteForAllInJob(
                            jobUid = uriToSaveQueueItems.firstOrNull()?.transferJobItem?.tjiTjUid ?: 0,
                            status = TransferJobItemStatus.FAILED.value
                        )
                    }
                }

                throw e
            }

        }
    }
}