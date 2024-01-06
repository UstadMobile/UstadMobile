package com.ustadmobile.core.domain.contententry.getmetadatafromuri

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.util.randomUuid
import kotlinx.serialization.json.Json
import web.url.URL
import com.ustadmobile.core.domain.contententry.getmetadatafromuri.IContentEntryGetMetaDataFromUriUseCase.Companion.HEADER_ORIGINAL_FILENAME
import com.ustadmobile.core.domain.upload.ChunkedUploadClientLocalUriUseCase
import com.ustadmobile.core.util.stringvalues.asIStringValues

/**
 * Javascript implementation of IContentEntryGetMetaDataFromUriUseCase . This will upload to the s
 * erver in chunks as required using ContentUploadRoute. Once the server has received the final chunk,
 * it will return the MetadataResult.
 *
 * The file object must be passed by the preceding screen using navResultReturner using the
 * RESULT_KEY_FILE .
 */
class ContentEntryGetMetaDataFromUriUseCaseJs(
    private val json: Json,
    private val chunkedUploadClientLocalUriUseCase: ChunkedUploadClientLocalUriUseCase,
) : IContentEntryGetMetaDataFromUriUseCase{
    override suspend fun invoke(
        contentUri: DoorUri,
        fileName: String?,
        endpoint: Endpoint,
        onProgress: (ContentEntryGetMetadataStatus) -> Unit
    ): MetadataResult {
        val uploadUuid = randomUuid().toString()

        try {
            val finalResponse = chunkedUploadClientLocalUriUseCase(
                uploadUuid = uploadUuid,
                localUri = contentUri,
                remoteUrl = "${endpoint.url}api/contentupload/upload",
                fromByte = 0,
                lastChunkHeaders = buildMap {
                    if(fileName != null)
                        put(HEADER_ORIGINAL_FILENAME, listOf(fileName))
                }.asIStringValues(),
                onProgress = {
                    onProgress(
                        ContentEntryGetMetadataStatus(
                            indeterminate = false,
                            progress = ((it.bytesTransferred * 100) / it.totalBytes).toInt()
                        )
                    )
                }
            )

            return finalResponse.body?.let {
                json.decodeFromString(
                    deserializer = MetadataResult.serializer(),
                    string = it
                )
            } ?: throw IllegalStateException("Final response had no body")
        }finally {
            URL.revokeObjectURL(contentUri.toString())
        }
    }

}