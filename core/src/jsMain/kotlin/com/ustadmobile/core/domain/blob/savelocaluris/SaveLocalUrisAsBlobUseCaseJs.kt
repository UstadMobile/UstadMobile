package com.ustadmobile.core.domain.blob.savelocaluris

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.transferjobitem.UpdateTransferJobItemEtagUseCase
import com.ustadmobile.core.domain.blob.upload.BlobUploadClientUseCase.Companion.BLOB_RESPONSE_HEADER_PREFIX
import com.ustadmobile.core.domain.upload.ChunkedUploadClientLocalUriUseCase
import com.ustadmobile.core.util.stringvalues.asIStringValues
import com.ustadmobile.core.util.uuid.randomUuidAsString
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.TransferJob
import com.ustadmobile.lib.db.entities.TransferJobItem
import io.github.aakira.napier.Napier
import js.promise.await
import kotlinx.serialization.json.Json
import web.http.fetchAsync

class SaveLocalUrisAsBlobUseCaseJs(
    private val chunkedUploadClientLocalUriUseCase: ChunkedUploadClientLocalUriUseCase,
    private val endpoint: Endpoint,
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
        localUrisToSave: List<SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem>
    ): List<SaveLocalUrisAsBlobsUseCase.SavedBlob> {
        val uriToSaveQueueItems = db.withDoorTransactionAsync {
            val transferJobUid = db.transferJobDao.insert(TransferJob()).toInt()

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
                val transferJobItemUid = db.transferJobItemDao.insert(transferJobItem).toInt()
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



        uriToSaveQueueItems.forEach {

        }


        return localUrisToSave.map { itemToSave ->
            Napier.d("SaveLocalUrisAsBlobUseCaseJs: uploading ${itemToSave.localUri}")
            val response = chunkedUploadClientLocalUriUseCase(
                uploadUuid = randomUuidAsString(),
                localUri = DoorUri.parse(itemToSave.localUri),
                remoteUrl = "${endpoint.url}api/blob/upload-item",
                lastChunkHeaders = buildMap {
                    itemToSave.mimeType?.also { blobMimeType ->
                        put("${BLOB_RESPONSE_HEADER_PREFIX}Content-Type", listOf(blobMimeType))
                    }
                }.asIStringValues(),
            )

            val responseJsonStr = response.body
                ?: throw IllegalStateException("SaveLocalUrisAsBlobUseCaseJs: no response body!")

            val serverSavedBlob = json.decodeFromString(
                SaveLocalUrisAsBlobsUseCase.ServerSavedBlob.serializer(), responseJsonStr
            )
            Napier.d("SaveLocalUrisAsBlobUseCaseJs: upload complete: ${itemToSave.localUri} " +
                    "stored as ${serverSavedBlob.blobUrl}")
            SaveLocalUrisAsBlobsUseCase.SavedBlob(
                entityUid = itemToSave.entityUid,
                localUri = itemToSave.localUri,
                blobUrl = serverSavedBlob.blobUrl,
            )
        }
    }
}