package com.ustadmobile.core.domain.upload

import com.ustadmobile.core.domain.blob.TransferJobItemStatus
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.util.ext.toMap
import com.ustadmobile.core.util.stringvalues.IStringValues
import com.ustadmobile.core.util.stringvalues.asIStringValues
import com.ustadmobile.door.DoorUri
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.ByteReadChannel
import kotlinx.io.readTo

/**
 * KTOR based implementation to upload binary content in chunks. See UploadRoute implementation
 * @param uriHelper system uri helper that will provide information on total size and can be used
 *        to get data
 */
class ChunkedUploadClientUseCaseKtorImpl(
    private val httpClient: HttpClient,
    private val uriHelper: UriHelper,
): ChunkedUploadClientLocalUriUseCase, ChunkedUploadClientChunkGetterUseCase {

    data class ChunkResponseInfo(
        val extraHeaders: Map<String, List<String>> = emptyMap(),
    )


    class LocalUriChunkGetter(
        private val localUri: DoorUri,
        private val uriHelper: UriHelper,
        private val finalHeaders: IStringValues? = null
    ): ChunkedUploadClientChunkGetterUseCase.UploadChunkGetter {
        @OptIn(ExperimentalStdlibApi::class)
        override suspend fun invoke(chunk: ChunkInfo.Chunk, buffer: ByteArray): ChunkResponseInfo? {
            uriHelper.openSource(localUri).use {
                it.skip(chunk.start)
                it.readTo(buffer, 0, chunk.size)
            }
            return if(chunk.isLastChunk && finalHeaders != null) {
                ChunkResponseInfo(finalHeaders.toMap())
            }else {
                null
            }
        }
    }


    /**
     * Upload data from a localUri in chunks
     *
     * @param uploadUuid UUID for this upload - should remain consistent if uploads are resumed
     * @param localUri the local URI (eg. file URI or URI returned from an Android file selector)
     * @param remoteUrl the endpoint to which the data will be uploaded (should use UploadRoute)
     * @param fromByte the first byte to start uploading from (e.g. use if resuming).
     * @param chunkSize the maximum size of each chunk
     * @param onProgress event handler that will be called each time a chunk is uploaded.
     * @param onStatusChange event handler that will be called when status changes e.g. on start,
     *        on successful completion, or on failure.
     */
    @OptIn(ExperimentalStdlibApi::class)
    override suspend operator fun invoke(
        uploadUuid: String,
        localUri: DoorUri,
        remoteUrl: String,
        fromByte: Long,
        chunkSize: Int,
        onProgress: (Long) -> Unit,
        onStatusChange: (TransferJobItemStatus) -> Unit,
        lastChunkHeaders: IStringValues?,
    ): ChunkedUploadClientLocalUriUseCase.LastChunkResponse {
        return invoke(
            uploadUuid = uploadUuid,
            totalSize = uriHelper.getSize(localUri),
            getChunk = LocalUriChunkGetter(localUri, uriHelper),
            remoteUrl = remoteUrl,
            chunkSize = chunkSize,
            fromByte =  fromByte,
            onProgress = onProgress,
            onStatusChange = onStatusChange,
        )
    }

    /**
     * Upload arbitrary binary data in chunks
     *
     * @param uploadUuid UUID for this upload - should remain consistent if uploads are resumed
     * @param totalSize the total size of the upload
     * @param getChunk a function that will fill the ByteArray with a chunk of data as per the Chunk params
     * @param remoteUrl the endpoint to which the data will be uploaded (should use UploadRoute)
     * @param fromByte the first byte to start uploading from (e.g. use if resuming).
     * @param chunkSize the maximum size of each chunk
     */
    override suspend operator fun invoke(
        uploadUuid: String,
        totalSize: Long,
        getChunk: ChunkedUploadClientChunkGetterUseCase.UploadChunkGetter,
        remoteUrl: String,
        fromByte: Long,
        chunkSize: Int,
        onProgress: (Long) -> Unit,
        onStatusChange: (TransferJobItemStatus) -> Unit,
    ): ChunkedUploadClientLocalUriUseCase.LastChunkResponse {
        if(totalSize <= 0)
            throw IllegalArgumentException("Upload size <= 0")

        val chunkInfo = ChunkInfo(
            totalSize = totalSize,
            chunkSize = chunkSize,
            fromByte = fromByte
        )

        Napier.d {
            "ChunkedUploadClientUseCase($uploadUuid): Uploading $totalSize bytes in " +
                    "${chunkInfo.numChunks} chunks to $remoteUrl starting from byte=$fromByte"
        }
        try {
            val buffer = ByteArray(chunkSize)
            onStatusChange(TransferJobItemStatus.IN_PROGRESS)
            chunkInfo.forEach { chunk ->
                val chunkResponseInfo = getChunk(chunk, buffer)
                val response = httpClient.post(remoteUrl) {
                    header(HEADER_UPLOAD_UUID, uploadUuid)
                    header(HEADER_IS_FINAL_CHUNK, chunk.isLastChunk.toString())
                    chunkResponseInfo?.extraHeaders?.forEach { extraHeader ->
                        extraHeader.value.forEach { headerVal ->
                            header(extraHeader.key, headerVal)
                        }
                    }

                    setBody(ByteReadChannel(buffer, 0, chunk.size))
                }
                onProgress(chunk.start + chunk.size)

                if(chunk.isLastChunk) {
                    Napier.d {
                        "ChunkedUploadClientUseCase($uploadUuid): Upload complete of $totalSize bytes in " +
                                "${chunkInfo.numChunks} chunks to $remoteUrl"
                    }
                    onStatusChange(TransferJobItemStatus.COMPLETE)
                    return ChunkedUploadClientLocalUriUseCase.LastChunkResponse(
                        body = if(response.status == HttpStatusCode.OK) {
                            response.bodyAsText()
                        }else {
                            null
                        },
                        statusCode = response.status.value,
                        headers = response.headers.asIStringValues(),
                    )
                }
            }
            throw IllegalStateException("Should have returned after lastChunk")
        }catch(e: Exception) {
            Napier.e("ChunkedUploadClientUseCase($uploadUuid): Exception uploading", e)
            onStatusChange(TransferJobItemStatus.FAILED)
            throw e
        }
    }

    companion object {

        const val DEFAULT_CHUNK_SIZE = 512 * 1024

    }
}