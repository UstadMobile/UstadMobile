package com.ustadmobile.core.domain.blob.savelocaluris

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.blob.upload.BlobUploadClientUseCase.Companion.BLOB_RESPONSE_HEADER_PREFIX
import com.ustadmobile.core.domain.upload.ChunkedUploadClientLocalUriUseCase
import com.ustadmobile.core.util.stringvalues.asIStringValues
import com.ustadmobile.core.util.uuid.randomUuidAsString
import com.ustadmobile.door.DoorUri
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.Json

class SaveLocalUrisAsBlobUseCaseJs(
    private val chunkedUploadClientLocalUriUseCase: ChunkedUploadClientLocalUriUseCase,
    private val endpoint: Endpoint,
    private val json: Json,
): SaveLocalUrisAsBlobsUseCase {

    override suspend fun invoke(
        localUrisToSave: List<SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem>
    ): List<SaveLocalUrisAsBlobsUseCase.SavedBlob> {
        //Could insert TransferJob(s) here

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