package com.ustadmobile.core.domain.upload

import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import com.ustadmobile.core.util.stringvalues.IStringValues
import com.ustadmobile.door.DoorUri

/**
 * Chunked upload client use case where data is read from a local URI. This can be a file or other
 * supported Uri on JVM/Android, or a blob/file URI on JS.
 */
interface ChunkedUploadClientLocalUriUseCase {

    data class UploadProgress(
        val bytesTransferred: Long,
        val totalBytes: Long,
    )

    /**
     * Response from the last chunk that was uploaded (e.g. when the upload was completed)
     */
    data class LastChunkResponse(
        val body: String?,
        val statusCode: Int,
        val headers: IStringValues,
    )

    /**
     * @param lastChunkHeaders headers that will be included on the last chunk uploaded. Can be used
     *        to send additional info that is used to complete processing once the upload is complete.
     */
    suspend operator fun invoke(
        uploadUuid: String,
        localUri: DoorUri,
        remoteUrl: String,
        fromByte: Long = 0,
        chunkSize: Int = DEFAULT_CHUNK_SIZE,
        onProgress: (UploadProgress) -> Unit = { },
        onStatusChange: (TransferJobItemStatus) -> Unit = { },
        lastChunkHeaders: IStringValues? = null,
    ): LastChunkResponse



}