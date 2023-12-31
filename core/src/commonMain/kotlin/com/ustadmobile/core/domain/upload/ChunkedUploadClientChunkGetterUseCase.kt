package com.ustadmobile.core.domain.upload

import com.ustadmobile.lib.db.composites.TransferJobItemStatus

/**
 * Chunked upload client use case where chunk data is provided by a function that fills a buffer.
 * Used by the BlobUploadClientUseCase to upload from the local http cache.
 */
interface ChunkedUploadClientChunkGetterUseCase {

    /**
     * Interface that is implemented to provide chunked data for upload e.g. from a local URI,
     * cache http response (via partial requests), etc.
     */
    interface UploadChunkGetter {
        suspend operator fun invoke(chunk: ChunkInfo.Chunk, buffer: ByteArray): ChunkedUploadClientUseCaseKtorImpl.ChunkResponseInfo?
    }

    suspend operator fun invoke(
        uploadUuid: String,
        totalSize: Long,
        getChunk: UploadChunkGetter,
        remoteUrl: String,
        fromByte: Long = 0,
        chunkSize: Int = DEFAULT_CHUNK_SIZE,
        onProgress: (Long) -> Unit = { },
        onStatusChange: (TransferJobItemStatus) -> Unit = { },
    ): ChunkedUploadClientLocalUriUseCase.LastChunkResponse

}