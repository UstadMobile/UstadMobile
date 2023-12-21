package com.ustadmobile.core.domain.upload

import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.door.DoorUri
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.utils.io.ByteReadChannel
import kotlinx.io.readTo

/**
 * KTOR based implementation to upload binary content in chunks. See UploadRoute implementation
 */
class ChunkedUploadUseCase(
    private val httpClient: HttpClient,
) {

    /**
     * Upload data from a localUri in chunks
     *
     * @param uploadUuid UUID for this upload - should remain consistent if uploads are resumed
     * @param localUri the local URI (eg. file URI or URI returned from an Android file selector)
     * @param uriHelper system uri helper that will provide information on total size and can be used
     *        to get data
     * @param remoteUrl the endpoint to which the data will be uploaded (should use UploadRoute)
     * @param fromByte the first byte to start uploading from (e.g. use if resuming).
     * @param chunkSize the maximum size of each chunk
     */
    @OptIn(ExperimentalStdlibApi::class)
    suspend operator fun invoke(
        uploadUuid: String,
        localUri: DoorUri,
        uriHelper: UriHelper,
        remoteUrl: String,
        fromByte: Long = 0,
        chunkSize: Int = DEFAULT_CHUNK_SIZE,
    ) {

        invoke(
            uploadUuid = uploadUuid,
            totalSize = uriHelper.getSize(localUri),
            getChunk = { chunk, buffer ->
                uriHelper.openSource(localUri).use {
                    it.skip(chunk.start)
                    it.readTo(buffer, 0, chunk.size)
                }
            },
            remoteUrl = remoteUrl,
            chunkSize = chunkSize,
            fromByte =  fromByte,
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
    suspend operator fun invoke(
        uploadUuid: String,
        totalSize: Long,
        getChunk: suspend (chunk: ChunkInfo.Chunk, buffer: ByteArray) -> Unit,
        remoteUrl: String,
        fromByte: Long = 0,
        chunkSize: Int = DEFAULT_CHUNK_SIZE
    ) {
        if(totalSize <= 0)
            throw IllegalArgumentException("Upload size <= 0")

        val chunkInfo = ChunkInfo(
            totalSize = totalSize,
            chunkSize = chunkSize,
            fromByte = fromByte
        )

        try {
            val buffer = ByteArray(chunkSize)
            chunkInfo.forEach { chunk ->
                getChunk(chunk, buffer)
                httpClient.post(remoteUrl) {
                    header(HEADER_UPLOAD_UUID, uploadUuid)
                    header(HEADER_IS_FINAL_CHUNK, chunk.isLastChunk.toString())
                    setBody(ByteReadChannel(buffer, 0, chunk.size))
                }
            }
        }catch(e: Exception) {
            throw e
        }
    }

    companion object {

        const val DEFAULT_CHUNK_SIZE = 512 * 1024

    }
}