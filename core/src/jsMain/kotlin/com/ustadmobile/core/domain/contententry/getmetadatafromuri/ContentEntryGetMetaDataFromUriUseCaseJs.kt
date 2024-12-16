package com.ustadmobile.core.domain.contententry.getmetadatafromuri

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.util.randomUuid
import kotlinx.serialization.json.Json
import web.url.URL
import com.ustadmobile.core.domain.contententry.getmetadatafromuri.ContentEntryGetMetaDataFromUriUseCase.Companion.HEADER_ORIGINAL_FILENAME
import com.ustadmobile.core.domain.upload.ChunkedUploadClientLocalUriUseCase
import com.ustadmobile.core.util.stringvalues.asIStringValues
import com.ustadmobile.door.util.encodeURIComponent

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
) : ContentEntryGetMetaDataFromUriUseCase{
    override suspend fun invoke(
        contentUri: DoorUri,
        fileName: String?,
        learningSpace: LearningSpace,
        onProgress: (ContentEntryGetMetadataStatus) -> Unit
    ): MetadataResult {
        val uploadUuid = randomUuid().toString()

        try {
            for(i in 0 until MAX_ATTEMPTS) {
                val finalResponse = chunkedUploadClientLocalUriUseCase(
                    uploadUuid = uploadUuid,
                    localUri = contentUri,
                    remoteUrl = "${learningSpace.url}api/contentupload/upload",
                    fromByte = 0,
                    lastChunkHeaders = buildMap {
                        if(fileName != null)
                            put(HEADER_ORIGINAL_FILENAME, listOf(encodeURIComponent(fileName)))
                    }.asIStringValues(),
                    onProgress = {
                        onProgress(
                            ContentEntryGetMetadataStatus(
                                indeterminate = false,
                                processedBytes = it.bytesTransferred,
                                totalBytes = it.totalBytes,
                            )
                        )
                    }
                )

                when(finalResponse.statusCode) {
                    400 -> {
                        //Invalid content
                        throw InvalidContentException(message = finalResponse.body ?: "")
                    }
                    406 -> {
                        //Unsupported content -
                        throw UnsupportedContentException(message = finalResponse.body ?: "")
                    }
                }

                return finalResponse.body?.let {
                    json.decodeFromString(
                        deserializer = MetadataResult.serializer(),
                        string = it
                    )
                } ?: throw IllegalStateException("Final response had no body")
            }
            throw IllegalStateException("Retried upload $MAX_ATTEMPTS times... failed.")
        }finally {
            URL.revokeObjectURL(contentUri.toString())
        }

    }

    companion object {

        const val MAX_ATTEMPTS = 3

    }

}