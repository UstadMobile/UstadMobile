package com.ustadmobile.core.domain.upload

import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import com.ustadmobile.core.util.stringvalues.IStringValues
import com.ustadmobile.core.util.stringvalues.asIStringValues
import com.ustadmobile.door.DoorUri
import io.github.aakira.napier.Napier
import js.core.jso
import js.promise.await
import web.http.fetch
import web.http.fetchAsync
import kotlin.js.json


class ChunkedUploadClientLocalUriUseCaseJs: ChunkedUploadClientLocalUriUseCase {

    override suspend fun invoke(
        uploadUuid: String,
        localUri: DoorUri,
        remoteUrl: String,
        fromByte: Long,
        chunkSize: Int,
        onProgress: (ChunkedUploadClientLocalUriUseCase.UploadProgress) -> Unit,
        onStatusChange: (TransferJobItemStatus) -> Unit,
        lastChunkHeaders: IStringValues?,
    ): ChunkedUploadClientLocalUriUseCase.LastChunkResponse {
        val logPrefix = "ChunkedUploadClientLocalUriUseCaseJs ($uploadUuid) : $localUri -> $remoteUrl:"
        try {
            Napier.d("$logPrefix : starting")
            val blob = fetch(localUri.uri.toString()).blob().await()
            val totalSize = blob.size
            if(totalSize <= 0)
                throw IllegalArgumentException("Upload size <= 0")

            val chunkInfo = ChunkInfo(
                totalSize = totalSize.toLong(),
                chunkSize = chunkSize,
                fromByte = fromByte
            )

            if(totalSize >= Int.MAX_VALUE)
                throw IllegalArgumentException("JS: upload size(${totalSize.toLong()}) > ${Int.MAX_VALUE} not supported")

            onStatusChange(TransferJobItemStatus.IN_PROGRESS)
            chunkInfo.forEachIndexed { index, chunk ->
                Napier.v { "$logPrefix : upload chunk #${index+1}/${chunkInfo.numChunks}" }
                val uploadChunkBlob = blob.slice(chunk.start.toInt(), chunk.end.toInt())
                val headerPairs = buildList {
                    add(HEADER_UPLOAD_UUID to uploadUuid)
                    add(HEADER_IS_FINAL_CHUNK to chunk.isLastChunk.toString())
                    add(HEADER_UPLOAD_START_BYTE to chunk.start.toString())

                    if(chunk.isLastChunk && lastChunkHeaders != null) {
                        lastChunkHeaders.names().forEach { headerName ->
                            lastChunkHeaders.getAll(headerName).forEach { headerVal ->
                                add(headerName to headerVal)
                            }
                        }
                    }
                }

                val fetchResponse = fetchAsync(
                    input = remoteUrl,
                    init = jso {
                        body = uploadChunkBlob
                        method = "POST"
                        headers = json(*headerPairs.toTypedArray())
                    }
                ).await()
                Napier.v { "$logPrefix : upload chunk #${index+1}/${chunkInfo.numChunks} complete " }
                onProgress(
                    ChunkedUploadClientLocalUriUseCase.UploadProgress(
                        bytesTransferred = chunk.end,
                        totalBytes = totalSize.toLong(),
                    )
                )

                if(chunk.isLastChunk) {
                    onStatusChange(TransferJobItemStatus.COMPLETE)
                    Napier.d("$logPrefix: Complete!")
                    /*
                     * As per
                     * https://fetch.spec.whatwg.org/#ref-for-dom-body-text%E2%91%A0
                     * If there is no body, then response body will be an empty byte array, e.g.
                     * empty string.
                     */
                    return ChunkedUploadClientLocalUriUseCase.LastChunkResponse(
                        body = if(fetchResponse.status != 204) {
                            fetchResponse.text().await()
                        }else {
                            null
                        },
                        statusCode = fetchResponse.status,
                        headers = fetchResponse.headers.asIStringValues(),
                    )
                }
            }

            throw IllegalStateException("$logPrefix should have returned with last chunk")
        }catch(e: Throwable) {
            Napier.e("$logPrefix, ChunkedUploadClientLocalUriUseCaseJs: Exception", e)
            onStatusChange(TransferJobItemStatus.FAILED)
            throw e
        }
    }


}