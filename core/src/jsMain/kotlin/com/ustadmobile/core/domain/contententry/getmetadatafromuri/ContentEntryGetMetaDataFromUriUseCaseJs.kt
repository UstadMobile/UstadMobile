package com.ustadmobile.core.domain.contententry.getmetadatafromuri

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.impl.nav.NavResultReturner
import com.ustadmobile.core.domain.upload.HEADER_IS_FINAL_CHUNK
import com.ustadmobile.core.domain.upload.HEADER_UPLOAD_UUID
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.util.randomUuid
import js.core.jso
import js.promise.await
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import web.file.File
import web.http.Headers
import web.http.fetchAsync
import web.url.URL
import kotlin.js.json
import kotlin.math.min
import com.ustadmobile.core.contentjob.InvalidContentException
import js.uri.encodeURIComponent

/**
 * Javascript implementation of IContentEntryGetMetaDataFromUriUseCase . This will upload to the s
 * erver in chunks as required using ContentUploadRoute. Once the server has received the final chunk,
 * it will return the MetadataResult.
 *
 * The file object must be passed by the preceding screen using navResultReturner using the
 * RESULT_KEY_FILE .
 */
class ContentEntryGetMetaDataFromUriUseCaseJs(
    private val navResultReturner: NavResultReturner,
    private val json: Json,
) : IContentEntryGetMetaDataFromUriUseCase{
    override suspend fun invoke(
        contentUri: DoorUri,
        endpoint: Endpoint,
        onProgress: (ContentEntryGetMetadataStatus) -> Unit
    ): MetadataResult {
        val file = navResultReturner.resultFlowForKey(RESULT_KEY_FILE).first().result as? File
            ?: throw IllegalArgumentException("No file available on $RESULT_KEY_FILE")
        val uploadUrl = "${endpoint.url}api/contentupload/upload"
        val uploadUuid = randomUuid().toString()
        val numChunks = (file.size.toInt() / CHUNK_SIZE).let {
            if(file.size.toInt().mod(CHUNK_SIZE) != 0) it + 1 else it
        }

        try {
            for(i in 0 until numChunks) {
                val start = i * CHUNK_SIZE
                val end = min(start + CHUNK_SIZE, file.size.toInt())
                val uploadBlob = file.slice(start.toDouble(), end.toDouble())
                val isLastChunk = (i == (numChunks - 1))

                val fetchResponse = fetchAsync(
                    input = if(isLastChunk) {
                        "$uploadUrl?originalFilename=${encodeURIComponent(file.name)}"
                    } else {
                        uploadUrl
                    },
                    init = jso {
                        body = uploadBlob
                        method = "POST"
                        headers = Headers(
                            init = json(
                                HEADER_UPLOAD_UUID to uploadUuid,
                                HEADER_IS_FINAL_CHUNK to isLastChunk.toString(),
                            )
                        )
                    }
                ).await()

                onProgress(
                    ContentEntryGetMetadataStatus(
                        indeterminate = false,
                        progress = ((i * 100) / numChunks)
                    )
                )

                if(isLastChunk) {
                    if(fetchResponse.status == 406) {
                        throw UnsupportedContentException(fetchResponse.text().await())
                    }else if(fetchResponse.status == 400) {
                        throw InvalidContentException(fetchResponse.text().await())
                    }

                    val metadaDataStr = fetchResponse.text().await()
                    return json.decodeFromString(
                        deserializer = MetadataResult.serializer(),
                        string = metadaDataStr,
                    )
                }
            }
        }finally {
            URL.revokeObjectURL(contentUri.toString())
        }


        throw IllegalStateException("Should have returned by now")

    }

    companion object {

        const val RESULT_KEY_FILE = "getMetadataFile"

        const val CHUNK_SIZE = (1024 * 1024) //1MB

    }
}